package com.banking.customerservice.service;

import com.banking.customerservice.dto.CreateCustomerRequest;
import com.banking.customerservice.dto.CreateCustomerResponse;

import java.util.List;

public interface CustomerService {
    CreateCustomerResponse createCustomer(CreateCustomerRequest request);
    CreateCustomerResponse getCustomer(Long customerId);
    List<CreateCustomerResponse> getAllCustomers();
    CreateCustomerResponse updateCustomer (Long customerId, CreateCustomerRequest request);
    void deleteCustomer(Long customerId);
}
