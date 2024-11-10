package com.ash.config;

import com.ash.dto.OrderRequestDto;
import com.ash.entity.PurchaseOrder;
import com.ash.event.OrderStatus;
import com.ash.event.PaymentStatus;
import com.ash.repository.OrderRepository;
import com.ash.service.OrderStatusPublisher;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrderStatusUpdateHandler {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusPublisher publisher;

    @Transactional
    public void updateOrder(int id, Consumer<PurchaseOrder> consumer){
        orderRepository.findById(id).ifPresent(
                consumer.andThen(this::updateOrder)
        );
    }

    private void updateOrder(PurchaseOrder purchaseOrder) {
        boolean isPaymentComplete = PaymentStatus.PAYMENT_COMPLETED.equals(purchaseOrder.getPaymentStatus());
        OrderStatus orderStatus = isPaymentComplete? OrderStatus.ORDER_COMPLETED : OrderStatus.ORDER_CANCELLED;
        purchaseOrder.setOrderStatus(orderStatus);
        if(!isPaymentComplete){
            publisher.publishOrderEvent(convertEntityToDto(purchaseOrder),orderStatus);
        }
    }

    private OrderRequestDto convertEntityToDto(PurchaseOrder purchaseOrder) {
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setOrderId(purchaseOrder.getId());
        orderRequestDto.setAmount(purchaseOrder.getPrice());
        orderRequestDto.setProductId(purchaseOrder.getProductId());
        orderRequestDto.setUserId(purchaseOrder.getUserId());
        return orderRequestDto;
    }
}
