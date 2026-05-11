package com.banking.customerservice.exception;

public class CustomerNotFoundException  extends  RuntimeException{
    public CustomerNotFoundException(Long customerId){
        super("Customer not found with ID: " + customerId);
    }
}
