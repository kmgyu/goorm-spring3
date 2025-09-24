package io.goorm.board.service;

import io.goorm.board.dto.order.OrderDto;
import io.goorm.board.dto.order.OrderSearchDto;
import io.goorm.board.entity.Order;
import io.goorm.board.exception.OrderNotFoundException;
import io.goorm.board.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderService {

    private final OrderMapper orderMapper;

    public List<OrderDto> findAll(OrderSearchDto searchDto) {
        return orderMapper.findAll(searchDto).stream()
                .map(this::convertToDto)
                .toList();
    }

    public long countAll(OrderSearchDto searchDto) {
        return orderMapper.countAll(searchDto);
    }

    public List<OrderDto> findPendingOrders() {
        return orderMapper.findPendingOrders().stream()
                .map(this::convertToDto)
                .toList();
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
}