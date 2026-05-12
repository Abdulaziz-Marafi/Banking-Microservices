package com.banking.accountservice.exception;

public class SalaryAccountExistsException extends RuntimeException{
    public SalaryAccountExistsException(Long customerId) {
        super("Customer " + customerId + " already has a salary account");
    }
}
