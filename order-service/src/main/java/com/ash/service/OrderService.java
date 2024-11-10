package com.ash.service;

import com.ash.dto.OrderRequestDto;
import com.ash.entity.PurchaseOrder;
import com.ash.event.OrderStatus;
import com.ash.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusPublisher orderStatusPublisher;

    @Transactional
    public PurchaseOrder createOrder(OrderRequestDto orderRequestDto) {
        PurchaseOrder purchaseOrder = orderRepository.save(convertDtoToEntity(orderRequestDto));
        orderRequestDto.setOrderId(purchaseOrder.getId());
        orderStatusPublisher.publishOrderEvent(orderRequestDto, OrderStatus.ORDER_CREATED);
        return purchaseOrder;
    }

    public List<PurchaseOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    private PurchaseOrder convertDtoToEntity(OrderRequestDto dto) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setProductId(dto.getProductId());
        purchaseOrder.setUserId(dto.getUserId());
        purchaseOrder.setPrice(dto.getAmount());
        purchaseOrder.setOrderStatus(OrderStatus.ORDER_CREATED);
        return purchaseOrder;
    }
}
