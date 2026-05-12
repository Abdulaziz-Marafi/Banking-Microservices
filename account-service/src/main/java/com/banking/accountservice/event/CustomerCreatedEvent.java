package com.banking.accountservice.event;

import com.banking.accountservice.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent {
    private Long customerId;
    private String name;
    private CustomerType customerType;
}
