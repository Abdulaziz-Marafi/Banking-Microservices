package com.banking.customerservice.exception;

public class DuplicateCustomerException extends RuntimeException{
    public DuplicateCustomerException(String legalId){
        super("Customer already exists with legal ID: "+ legalId);
    }
}
