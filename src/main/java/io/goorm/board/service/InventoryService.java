package io.goorm.board.service;

import io.goorm.board.entity.Inventory;
import io.goorm.board.entity.Product;
import io.goorm.board.exception.InsufficientStockException;
import io.goorm.board.mapper.InventoryMapper;
import io.goorm.board.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;

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
}