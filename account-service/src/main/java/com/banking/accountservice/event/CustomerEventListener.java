package com.banking.accountservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEventListener {

    @RabbitListener(queues = "customer.created.queue")
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("Received CustomerCreatedEvent for customer: {} name: {} type: {}",
                event.getCustomerId(),
                event.getName(),
                event.getCustomerType());
    }
}
