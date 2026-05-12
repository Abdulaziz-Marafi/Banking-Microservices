package com.banking.accountservice.dto;

import com.banking.accountservice.enums.AccountStatus;
import com.banking.accountservice.enums.AccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateAccountResponse {

    private Long accountId;
    private Long customerId;
    private BigDecimal balance;
    private AccountStatus status;
    private AccountType type;
    private LocalDateTime createdAt;

}
