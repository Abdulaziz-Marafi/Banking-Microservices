package com.banking.accountservice.exception;

import com.banking.accountservice.enums.AccountStatus;

public class AccountNotActiveException extends RuntimeException {
    public AccountNotActiveException(Long accountId, AccountStatus status) {
        super("Account " + accountId + " is not active (status=" + status + ")");
    }
}
