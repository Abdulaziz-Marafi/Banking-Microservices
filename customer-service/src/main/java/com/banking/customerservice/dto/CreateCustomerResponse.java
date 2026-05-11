package com.banking.customerservice.dto;

import com.banking.customerservice.enums.CustomerType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// mobile and email can be null
@Data
@Builder
public class CreateCustomerResponse {

    private Long customerId;
    private String name;
    private String legalId;
    private CustomerType type;
    private String address;
    private String email;
    private String phone;
    private LocalDateTime createdAt;

}
