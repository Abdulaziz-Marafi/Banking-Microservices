package com.banking.customerservice.event;

import com.banking.customerservice.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Event to be sent to RabbitMQ and be read by accountService

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent {
    private Long customerId;
    private String name;
    private CustomerType customerType;
}
