package io.goorm.board.service;

import io.goorm.board.dto.order.OrderCreateDto;
import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderItemCreateDto;
import io.goorm.board.dto.order.OrderProductSelectionDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.Order;
import io.goorm.board.entity.OrderItem;
import io.goorm.board.entity.Product;
import io.goorm.board.entity.User;
import io.goorm.board.exception.InsufficientStockException;
import io.goorm.board.exception.OrderItemsNotSelectedException;
import io.goorm.board.exception.OrderNotFoundException;
import io.goorm.board.exception.InvalidUserRoleException;
import io.goorm.board.exception.CompanyNotFoundException;
import io.goorm.board.exception.product.ProductNotFoundException;
import io.goorm.board.enums.UserRole;
import io.goorm.board.mapper.OrderItemMapper;
import io.goorm.board.mapper.OrderMapper;
import io.goorm.board.mapper.ProductMapper;
import io.goorm.board.annotation.LogExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final InventoryService inventoryService;
    private final DiscountService discountService;

    @Transactional(rollbackFor = Exception.class)
    @LogExecution(operation = "CREATE_ORDER", resource = "ORDER")
    public OrderDto createOrder(OrderCreateDto createDto, User user) {
        // 사용자 권한 검증 (바이어만 주문 가능)
        if (user.getRole() != UserRole.BUYER) {
            throw new InvalidUserRoleException();
        }

        // 회사 정보 검증
        if (user.getCompanySeq() == null) {
            throw new CompanyNotFoundException();
        }

        // selectedProducts를 items로 변환 (새로운 폼 방식 지원)
        log.info("selectedProducts: {}", createDto.getSelectedProducts());
        if (createDto.getSelectedProducts() != null && !createDto.getSelectedProducts().isEmpty()) {
            log.info("Converting selectedProducts to items...");
            List<OrderItemCreateDto> items = createDto.getSelectedProducts().stream()
                    .filter(OrderProductSelectionDto::isSelected)
                    .map(selection -> {
                        // 상품 정보 조회
                        Product product = productMapper.findById(selection.getProductSeq())
                                .orElseThrow(() -> new ProductNotFoundException(selection.getProductSeq()));

                        // 재고 체크
                        inventoryService.checkStock(selection.getProductSeq(), selection.getQuantity());

                        return OrderItemCreateDto.builder()
                                .productSeq(selection.getProductSeq())
                                .quantity(selection.getQuantity())
                                .unitPrice(product.getUnitPrice()) // DB에서 조회한 실제 단가
                                .build();
                    })
                    .toList();
            log.info("Converted items: {}", items);
            createDto.setItems(items);
        } else {
            log.warn("selectedProducts is null or empty");
        }

        // items 검증 (selectedProducts 변환 후)
        if (createDto.getItems() == null || createDto.getItems().isEmpty()) {
            throw new OrderItemsNotSelectedException();
        }

        // 발주번호 생성
        String orderNumber = generateOrderNumber();

        // 할인율 계산 - 예외 처리 추가
        BigDecimal discountRate;
        try {
            BigDecimal calculatedRate = discountService.calculateDiscountRate(user.getCompanySeq());
            discountRate = (calculatedRate != null) ? calculatedRate : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("할인율 계산 실패, 0%로 설정. companySeq: {}, error: {}", user.getCompanySeq(), e.getMessage());
            discountRate = BigDecimal.ZERO;
        }
        final BigDecimal finalDiscountRate = discountRate;

        // 금액 계산
        BigDecimal totalAmount = calculateTotalAmount(createDto);
        BigDecimal discountAmount = totalAmount.multiply(discountRate).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // 발주 등록
        Order order = Order.builder()
                .companySeq(user.getCompanySeq())
                .userSeq(user.getUserSeq())
                .orderNumber(orderNumber)
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();

        order.approve("SYSTEM"); // 자동 승인
        orderMapper.insert(order);

        // 발주 상품 등록 (단순화: 할인은 order 레벨에서만 적용)
        List<OrderItem> orderItems = createDto.getItems().stream()
                .map(itemDto -> {
                    BigDecimal lineTotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));

                    return OrderItem.builder()
                            .orderSeq(order.getOrderSeq())
                            .productSeq(itemDto.getProductSeq())
                            .quantity(itemDto.getQuantity())
                            .unitPrice(itemDto.getUnitPrice())
                            .build();
                })
                .toList();

        orderItemMapper.insertBatch(orderItems);

        // 주문 승인 후 재고 예약
        createDto.getItems().forEach(itemDto -> {
            inventoryService.reserveStock(itemDto.getProductSeq(), itemDto.getQuantity());
        });

        return convertToDto(orderMapper.findById(order.getOrderSeq()).orElseThrow());
    }

    public List<OrderDto> findByCompany(Long companySeq, OrderSearchDto searchDto) {
        return orderMapper.findByCompanySeq(companySeq, searchDto).stream()
                .map(this::convertToDto)
                .toList();
    }

    public List<OrderDto> findAll(OrderSearchDto searchDto) {
        return orderMapper.findAll(searchDto).stream()
                .map(this::convertToDto)
                .toList();
    }

    public OrderDto findById(Long orderSeq) {
        Order order = orderMapper.findById(orderSeq)
                .orElseThrow(() -> new OrderNotFoundException());

        OrderDto orderDto = convertToDto(order);

        // 발주 상품 목록 조회
        List<OrderItem> orderItems = orderItemMapper.findByOrderSeq(orderSeq);
        orderDto.setOrderItems(orderItems.stream()
                .map(this::convertToItemDto)
                .toList());

        return orderDto;
    }

    /**
     * 배송 완료 처리
     */
    @Transactional(rollbackFor = Exception.class)
    @LogExecution(operation = "COMPLETE_DELIVERY", resource = "ORDER")
    public OrderDto completeDelivery(Long orderSeq, User user) {
        // 주문 조회
        Order order = orderMapper.findById(orderSeq)
                .orElseThrow(() -> new OrderNotFoundException());

        // 배송 완료 처리 (인보이스 자동 확정)
        order.completeDelivery(user.getUserSeq(), user.getEmail());
        orderMapper.updateDeliveryComplete(order);

        // 주문 항목별 재고 소모 처리
        List<OrderItem> orderItems = orderItemMapper.findByOrderSeq(orderSeq);
        orderItems.forEach(item -> {
            inventoryService.consumeStock(item.getProductSeq(), item.getQuantity());
        });

        // 출고 이력 기록 (신규 추가)
        inventoryService.recordOrderConsumption(order, user.getUserSeq(), user.getEmail());

        log.info("배송 완료 및 인보이스 확정 완료 - 주문: {}, 사용자: {}, 입금예정일: {}",
                orderSeq, user.getEmail(), order.getPaymentDueDate());

        return convertToDto(orderMapper.findById(orderSeq).orElseThrow());
    }


    /**
     * 배송 시작 처리
     */
    @Transactional(rollbackFor = Exception.class)
    @LogExecution(operation = "START_DELIVERY", resource = "ORDER")
    public OrderDto startDelivery(Long orderSeq, User user) {
        // 주문 조회
        Order order = orderMapper.findById(orderSeq)
                .orElseThrow(() -> new OrderNotFoundException());

        // 배송 시작 처리
        order.startDelivery("ADMIN");
        orderMapper.updateDeliveryComplete(order);

        log.info("배송 시작 처리됨 - 주문: {}, 사용자: {}", orderSeq, user.getEmail());

        return convertToDto(orderMapper.findById(orderSeq).orElseThrow());
    }

    /**
     * 입금 완료 처리
     */
    @Transactional(rollbackFor = Exception.class)
    @LogExecution(operation = "COMPLETE_PAYMENT", resource = "ORDER")
    public OrderDto completePayment(Long orderSeq, User user) {
        // 주문 조회
        Order order = orderMapper.findById(orderSeq)
                .orElseThrow(() -> new OrderNotFoundException());

        // 입금 완료 처리
        order.completePayment(user.getUserSeq(), user.getEmail());
        orderMapper.updatePaymentComplete(order);

        log.info("입금 완료 처리됨 - 주문: {}, 사용자: {}", orderSeq, user.getEmail());

        return convertToDto(orderMapper.findById(orderSeq).orElseThrow());
    }

    /**
     * 인보이스 PDF 생성
     */
    private void generateInvoice(Long orderSeq) {
        try {
            // TODO: PDF 생성 로직 구현
            // 1. 주문 정보 조회
            // 2. 회사 정보 조회
            // 3. 주문 항목 조회
            // 4. PDF 템플릿으로 인보이스 생성
            // 5. 파일 저장 또는 이메일 발송

            log.info("인보이스 생성 완료 - 주문: {}", orderSeq);
        } catch (Exception e) {
            log.error("인보이스 생성 실패 - 주문: {}, 오류: {}", orderSeq, e.getMessage());
            // 인보이스 생성 실패해도 배송 완료 처리는 계속 진행
        }
    }

    private BigDecimal calculateTotalAmount(OrderCreateDto createDto) {
        return createDto.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = orderMapper.findMaxDailySequence(datePrefix) + 1;
        return String.format("%s%04d", datePrefix, sequence);
    }

    private OrderDto convertToDto(Order order) {
        return OrderDto.builder()
                .orderSeq(order.getOrderSeq())
                .companySeq(order.getCompanySeq())
                .companyName(order.getCompanyName())
                .userSeq(order.getUserSeq())
                .userName(order.getUserName())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .deliveryStatus(order.getDeliveryStatus())
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .discountRate(order.getDiscountRate())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .build();
    }

    private io.goorm.board.dto.order.OrderItemDto convertToItemDto(OrderItem orderItem) {
        return io.goorm.board.dto.order.OrderItemDto.builder()
                .orderItemSeq(orderItem.getOrderItemSeq())
                .orderSeq(orderItem.getOrderSeq())
                .productSeq(orderItem.getProductSeq())
                .productName(orderItem.getProductName())
                .productCode(orderItem.getProductCode())
                .categoryName(orderItem.getCategoryName())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice())
                .build();
    }
}