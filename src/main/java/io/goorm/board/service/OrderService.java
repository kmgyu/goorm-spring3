package io.goorm.board.service;

import io.goorm.board.dto.order.*;
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

        // 주문 승인 후 재고 차감
        createDto.getItems().forEach(itemDto -> {
            inventoryService.decreaseStock(itemDto.getProductSeq(), itemDto.getQuantity());
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

    private OrderItemDto convertToItemDto(OrderItem orderItem) {
        return OrderItemDto.builder()
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