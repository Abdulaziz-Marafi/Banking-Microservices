package com.banking.accountservice.service;

import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.CreateAccountResponse;

import java.util.List;

public interface AccountService {
    CreateAccountResponse createAccount(CreateAccountRequest request);
    CreateAccountResponse getAccount(Long accountId);
    List<CreateAccountResponse> getAccountsByCustomer(Long customerId);
    CreateAccountResponse updateAccountStatus(Long accountId, String status);
    void deleteAccount(Long accountId);

}
