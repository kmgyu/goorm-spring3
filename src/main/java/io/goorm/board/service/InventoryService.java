package io.goorm.board.service;

import io.goorm.board.dto.excel.ExcelStockDto;
import io.goorm.board.entity.Inventory;
import io.goorm.board.entity.Product;
import io.goorm.board.entity.InventoryTransaction;
import io.goorm.board.entity.Order;
import io.goorm.board.entity.OrderItem;
import io.goorm.board.entity.User;
import io.goorm.board.enums.TransactionType;
import io.goorm.board.exception.InsufficientStockException;
import io.goorm.board.mapper.CategoryMapper;
import io.goorm.board.mapper.InventoryMapper;
import io.goorm.board.mapper.InventoryTransactionMapper;
import io.goorm.board.mapper.OrderItemMapper;
import io.goorm.board.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final OrderItemMapper orderItemMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 재고 확인 및 검증
     */
    public void checkStock(Long productSeq, Integer quantity) {
        // 재고 체크
        Inventory inventory = inventoryMapper.findByProductSeq(productSeq)
                .orElse(null);

        if (inventory == null || !inventory.canOrder(quantity)) {
            // 상품명 조회 (예외 메시지용)
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            int available = (inventory != null && inventory.getAvailableStock() != null)
                    ? inventory.getAvailableStock() : 0;

            throw new InsufficientStockException(productName, quantity, available);
        }
    }

    /**
     * 재고 차감 (주문 승인 시)
     */
    @Transactional
    public void decreaseStock(Long productSeq, Integer quantity) {
        int updatedRows = inventoryMapper.decreaseStock(productSeq, quantity);

        if (updatedRows == 0) {
            // 재고 차감 실패 (재고 부족 또는 상품 없음)
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            Inventory inventory = inventoryMapper.findByProductSeq(productSeq).orElse(null);
            int available = (inventory != null && inventory.getCurrentStock() != null)
                    ? inventory.getCurrentStock() : 0;

            throw new InsufficientStockException(productName, quantity, available);
        }

        log.info("재고 차감 완료 - 상품: {}, 수량: {}", productSeq, quantity);
    }

    /**
     * 재고 예약 (주문 시)
     */
    @Transactional
    public void reserveStock(Long productSeq, Integer quantity) {
        int updatedRows = inventoryMapper.reserveStock(productSeq, quantity);

        if (updatedRows == 0) {
            // 재고 예약 실패 (재고 부족 또는 상품 없음)
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            Inventory inventory = inventoryMapper.findByProductSeq(productSeq).orElse(null);
            int available = (inventory != null && inventory.getAvailableStock() != null)
                    ? inventory.getAvailableStock() : 0;

            throw new InsufficientStockException(productName, quantity, available);
        }

        log.info("재고 예약 완료 - 상품: {}, 수량: {}", productSeq, quantity);
    }

    /**
     * 재고 소모 처리 (배송 완료 시)
     */
    @Transactional
    public void consumeStock(Long productSeq, Integer quantity) {
        int updatedRows = inventoryMapper.consumeStock(productSeq, quantity);

        if (updatedRows == 0) {
            // 재고 소모 실패
            Product product = productMapper.findById(productSeq).orElse(null);
            String productName = (product != null) ? product.getName() : "Unknown Product";

            throw new InsufficientStockException(productName, quantity, 0);
        }

        log.info("재고 소모 완료 - 상품: {}, 수량: {}", productSeq, quantity);
    }

    /**
     * 엑셀 입고 처리 (파일 정보 및 공급업체 정보 포함)
     */
    @Transactional
    public List<String> processStockReceiving(List<ExcelStockDto> stockList, Long supplierSeq,
                                              User user, String excelFilename, String excelFilepath) {
        List<String> errors = new ArrayList<>();
        LocalDateTime processedAt = LocalDateTime.now();

        for (ExcelStockDto dto : stockList) {
            try {
                // 1. 상품코드로 상품 조회 및 검증
                Product product = validateAndFindProduct(dto.getProductCode(), supplierSeq, dto.getRowNumber());

                // 2. 재고 업데이트
                updateStock(product.getProductSeq(), dto.getQuantity());

                // 3. 입고 이력 저장
                InventoryTransaction transaction = InventoryTransaction.builder()
                        .transactionType(TransactionType.RECEIVING)
                        .productSeq(product.getProductSeq())
                        .categorySeq(product.getCategorySeq())
                        .quantity(dto.getQuantity())
                        .unitPrice(dto.getUnitPrice())
                        .totalAmount(dto.getUnitPrice().multiply(java.math.BigDecimal.valueOf(dto.getQuantity())))
                        .note(dto.getNote())
                        .processedBySeq(user.getUserSeq())
                        .excelFilename(excelFilename)
                        .excelFilepath(excelFilepath)
                        .excelRowNum(dto.getRowNumber())
                        .processedAt(processedAt)
                        .build();

                inventoryTransactionMapper.insert(transaction);

                log.info("입고 처리 완료 - {}행: {} ({}) +{} (이력ID: {})",
                        dto.getRowNumber(), dto.getProductName(), dto.getCategoryName(), dto.getQuantity(),
                        transaction.getTransactionSeq());

            } catch (Exception e) {
                String errorMsg = String.format("%d행: %s", dto.getRowNumber(), e.getMessage());
                errors.add(errorMsg);
                log.error("입고 처리 실패 - {}", errorMsg, e);
            }
        }

        log.info("엑셀 입고 처리 완료 - 총 {}건, 성공 {}건, 실패 {}건, 파일: {}",
                stockList.size(), stockList.size() - errors.size(), errors.size(), excelFilename);

        return errors;
    }

    /**
     * 엑셀 입고 처리 (기존 호환성 유지)
     */
    @Transactional
    public List<String> processStockReceiving(List<ExcelStockDto> stockList, User user) {
        return processStockReceiving(stockList, null, user, "unknown.xlsx", "");
    }


    /**
     * 재고 업데이트 (입고 처리)
     */
    @Transactional
    public void updateStock(Long productSeq, Integer quantity) {
        // 기존 재고 조회
        Optional<Inventory> existingInventory = inventoryMapper.findByProductSeq(productSeq);

        if (existingInventory.isPresent()) {
            // 기존 재고에 추가
            int updatedRows = inventoryMapper.increaseStock(productSeq, quantity);
            if (updatedRows == 0) {
                throw new RuntimeException("재고 업데이트에 실패했습니다.");
            }
            log.debug("기존 재고 업데이트 - 상품: {}, 추가수량: {}", productSeq, quantity);
        } else {
            // 새로운 재고 생성
            Inventory newInventory = Inventory.builder()
                    .productSeq(productSeq)
                    .currentStock(quantity)
                    .reservedStock(0)
                    .build();

            inventoryMapper.insert(newInventory);
            log.debug("신규 재고 생성 - 상품: {}, 초기수량: {}", productSeq, quantity);
        }
    }

    /**
     * 주문 출고 이력 기록 (배송완료 시 호출)
     */
    @Transactional
    public void recordOrderConsumption(Order order, Long processedBySeq, String processedBy) {
        log.info("주문 출고 이력 기록 시작 - 주문: {}, 처리자: {}", order.getOrderNumber(), processedBy);

        // 주문 상품들 조회
        List<OrderItem> orderItems = orderItemMapper.findByOrderSeq(order.getOrderSeq());

        for (OrderItem item : orderItems) {
            // 상품 정보 조회하여 categorySeq 획득
            Product product = productMapper.findById(item.getProductSeq()).orElse(null);
            Long categorySeq = (product != null) ? product.getCategorySeq() : null;

            // 출고 이력 생성
            InventoryTransaction transaction = InventoryTransaction.builder()
                    .transactionType(TransactionType.ORDER_CONSUMED)
                    .productSeq(item.getProductSeq())
                    .categorySeq(categorySeq)
                    .quantity(-item.getQuantity())  // 출고는 음수로 기록
                    .unitPrice(item.getUnitPrice())
                    .totalAmount(item.getLineTotal().negate())  // 음수로 기록
                    .orderSeq(order.getOrderSeq())
                    .processedBySeq(processedBySeq)
                    .note(String.format("주문 %s 배송완료 - %s", order.getOrderNumber(), processedBy))
                    .processedAt(LocalDateTime.now())
                    .build();

            inventoryTransactionMapper.insert(transaction);

            log.debug("출고 이력 기록 완료 - 주문: {}, 상품: {}, 수량: {} (이력ID: {})",
                    order.getOrderNumber(), item.getProductSeq(), item.getQuantity(),
                    transaction.getTransactionSeq());
        }

        log.info("주문 출고 이력 기록 완료 - 주문: {}, 상품 {}건 처리", order.getOrderNumber(), orderItems.size());
    }

    /**
     * 상품코드로 상품 조회 및 기본 검증 (강사 구현)
     */
    private Product validateAndFindProduct(String productCode, Long selectedSupplierSeq, int rowNumber) {
        try {
            // 1. 상품코드로 상품 조회 (필수 구현)
            Product product = productMapper.findByCode(productCode)
                    .orElseThrow(() -> new RuntimeException(
                            String.format("%d행: 상품코드 '%s'를 찾을 수 없습니다", rowNumber, productCode)));

            // 2. 상품 상태 확인 (필수 구현)
            // TODO: 임시로 상태 검증 비활성화 - DB 데이터 확인 후 재활성화 예정
            log.debug("{}행: 상품 상태 - 코드: {}, 상태: {}", rowNumber, productCode, product.getStatus());
            /*
            if (!"ACTIVE".equals(product.getStatus())) {
                throw new RuntimeException(
                    String.format("%d행: 상품코드 '%s'는 비활성 상태입니다", rowNumber, productCode));
            }
            */

            // 3. 공급업체 매칭 검증 (수강생 과제)
            validateProductSupplierMatch(product, selectedSupplierSeq, rowNumber);

            return product;

        } catch (Exception e) {
            log.error("상품 검증 실패 - {}행: {}", rowNumber, e.getMessage());
            throw e;
        }
    }

    /**
     * 상품과 선택된 공급업체의 일치성을 검증합니다.
     *
     * TODO: 수강생 구현 과제
     *
     * 구현 가이드:
     * 1. 상품의 실제 공급업체(product.getSupplierSeq())와 선택된 공급업체 비교
     * 2. 불일치 시 처리 방법 결정:
     *    - 경고 로그만 출력하고 계속 진행
     *    - 예외 발생하여 해당 행 처리 중단
     *    - 별도 검증 결과 수집하여 최종 리포트에 포함
     * 3. 추가 검증 로직:
     *    - 공급업체별 상품 공급 정책 확인
     *    - 계약 유효성 검증
     *    - 최소/최대 주문 수량 확인
     *
     * @param product 조회된 상품 정보
     * @param selectedSupplierSeq 사용자가 선택한 공급업체 ID
     * @param rowNumber 엑셀 행 번호 (에러 메시지용)
     */
    private void validateProductSupplierMatch(Product product, Long selectedSupplierSeq, int rowNumber) {
        // TODO: 수강생 구현 과제 - 이 메서드의 바디를 수정하여 검증 로직을 완성하세요
        //
        // 현재 상황:
        // - 공급업체 선택은 필수 (컨트롤러에서 검증됨)
        // - 잘못된 공급업체를 선택해도 입고 처리는 정상 동작함
        // - 수강생이 검증 로직을 추가하면, 그 시점에 오류 처리 및 화면에 오류 메시지 표시
        //
        // 구현 과제:
        // 1. 상품-공급업체 매칭 검증
        //    - product.getSupplierSeq()와 selectedSupplierSeq를 비교
        //    - 불일치 시 처리 방법 결정:
        //      a) 경고 로그만 출력하고 계속 진행 (현재 방식)
        //      b) 예외 발생하여 해당 행 처리 중단 → 화면에 오류 메시지 표시
        //      c) 별도 검증 결과 수집하여 최종 리포트에 포함
        //
        // 2. 추가 비즈니스 로직 (선택사항)
        //    - 공급업체별 상품 공급 정책 확인
        //    - 계약 유효성 검증 (계약 기간, 계약 상태 등)
        //    - 최소/최대 주문 수량 확인
        //    - 가격 정책 검증 (공급업체별 단가 정책 등)
        //    - 공급업체 상태 확인 (활성/비활성 등)
        //
        // 예시 구현 (주석을 해제하고 수정하세요):
        /*
        // 공급업체 매칭 검증
        if (!product.getSupplierSeq().equals(selectedSupplierSeq)) {
            // 방법 1: 경고만 하고 계속 (관대한 정책)
            log.warn("{}행: 공급업체 불일치 - 상품: {} (상품 공급업체: {}, 선택 공급업체: {})",
                    rowNumber, product.getCode(), product.getSupplierSeq(), selectedSupplierSeq);

            // 방법 2: 엄격한 정책 - 예외 발생하여 처리 중단
            // throw new RuntimeException(
            //     String.format("상품 '%s'의 공급업체가 일치하지 않습니다. (상품 공급업체: %d, 선택 공급업체: %d)",
            //         product.getCode(), product.getSupplierSeq(), selectedSupplierSeq));
        }

        // 추가 검증 예시
        // if (product.getStatus() != null && !"ACTIVE".equals(product.getStatus())) {
        //     throw new RuntimeException("비활성 상품은 입고할 수 없습니다: " + product.getCode());
        // }
        */

        // 현재는 로그만 출력 (시연용 - 모든 데이터가 정상 처리됨)
        // 수강생 구현 후에는 실제 검증 로직에 따라 예외 발생 가능
        log.debug("{}행: 공급업체 검증 통과 - 상품코드: {}, 상품공급업체: {}, 선택공급업체: {}",
                rowNumber, product.getCode(), product.getSupplierSeq(), selectedSupplierSeq);
    }

}