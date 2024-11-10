package com.ash.service;

import com.ash.dto.OrderRequestDto;
import com.ash.dto.PaymentRequestDto;
import com.ash.entity.UserBalance;
import com.ash.entity.UserTransction;
import com.ash.event.OrderEvent;
import com.ash.event.PaymentEvent;
import com.ash.event.PaymentStatus;
import com.ash.repository.UserBalanceRepository;
import com.ash.repository.UserTransactionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @PostConstruct
    public void initUserBalanceInDB() {
        userBalanceRepository.saveAll(List.of(new UserBalance(101, 5000),
                new UserBalance(103, 5000),
                new UserBalance(102, 5000),
                new UserBalance(104, 5000)));
    }

    @Transactional
    public PaymentEvent newOrderEvent(OrderEvent orderEvent) {
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(orderRequestDto.getOrderId(),
                orderRequestDto.getUserId(),
                orderRequestDto.getAmount());
        return userBalanceRepository.findById(orderRequestDto.getUserId())
                .filter(ub -> ub.getPrice() > orderRequestDto.getAmount())
                .map(ub -> {
                    ub.setPrice(ub.getPrice() - orderRequestDto.getAmount());
                    userTransactionRepository.save(
                            new UserTransction(orderRequestDto.getOrderId(), orderRequestDto.getUserId(), orderRequestDto.getAmount())
                    );
                    return new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_COMPLETED);
                }).orElse(
                        new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_FAILED)
                );

    }

    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {

        userTransactionRepository.findById(orderEvent.getOrderRequestDto().getUserId())
                .ifPresent(ut->{
                    userTransactionRepository.delete(ut);
                    userTransactionRepository.findById(ut.getUserId())
                            .ifPresent(ub-> ub.setAmount(ub.getAmount()+ut.getAmount()));
                });
    }
}
