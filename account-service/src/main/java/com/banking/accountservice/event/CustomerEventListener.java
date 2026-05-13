package com.banking.accountservice.event;

import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.enums.AccountType;
import com.banking.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerEventListener {

    private final AccountService accountService;

    @RabbitListener(queues = "customer.created.queue")
    public void handleCustomerCreated(CustomerCreatedEvent event) {
        log.info("Received CustomerCreatedEvent for customer: {} name: {} type: {}",
                event.getCustomerId(), event.getName(), event.getCustomerType());

        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(event.getCustomerId());
        request.setType(AccountType.SAVINGS);
        request.setInitialBalance(BigDecimal.ZERO);

        accountService.createAccount(request);
        log.info("Auto-created default SAVINGS account for customer: {}", event.getCustomerId());
    }
}
