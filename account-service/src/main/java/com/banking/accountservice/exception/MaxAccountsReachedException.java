package com.banking.accountservice.exception;

public class MaxAccountsReachedException extends RuntimeException{
    public MaxAccountsReachedException(Long customerId) {
        super("Customer " + customerId + " has reached the maximum of 10 accounts");
    }
}
