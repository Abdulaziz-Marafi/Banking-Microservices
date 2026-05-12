package com.banking.customerservice.exception;

public class DuplicateCustomerException extends RuntimeException{
    public DuplicateCustomerException(String civilId){
        super("Customer already exists with Civil ID: "+ civilId);
    }
}
