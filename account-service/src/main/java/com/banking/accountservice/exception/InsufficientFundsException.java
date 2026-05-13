package com.banking.accountservice.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long accountId, BigDecimal balance, BigDecimal amount) {
        super("Account " + accountId + " has insufficient funds: balance=" + balance + ", requested=" + amount);
    }
}
