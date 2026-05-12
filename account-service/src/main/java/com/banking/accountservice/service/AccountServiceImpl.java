package com.banking.accountservice.service;

import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.CreateAccountResponse;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.enums.AccountStatus;
import com.banking.accountservice.enums.AccountType;
import com.banking.accountservice.event.AccountCreatedEvent;
import com.banking.accountservice.exception.AccountNotFoundException;
import com.banking.accountservice.exception.MaxAccountsReachedException;
import com.banking.accountservice.exception.SalaryAccountExistsException;
import com.banking.accountservice.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "banking.exchange";
    private static final String ACCOUNT_ROUTING_KEY = "account.created";
    private static final int MAX_ACCOUNTS = 10;

    @Override
    @Transactional
    public CreateAccountResponse createAccount (CreateAccountRequest request){
        log.info("Creating account for customer: {}", request.getCustomerId());

        // Enforce max 10 accounts per customer
        long accountCount = accountRepository.countByCustomerId(request.getCustomerId());
        if (accountCount >= MAX_ACCOUNTS) {
            throw new MaxAccountsReachedException(request.getCustomerId());
        }

        // Enforce one salary account per customer
        if (request.getType() == AccountType.SALARY && accountRepository.existsByCustomerIdAndType(
                request.getCustomerId(), AccountType.SALARY
        )){
            throw new SalaryAccountExistsException(request.getCustomerId());
        }

        Account account = Account.builder()
                .accountId(generateAccountId(request.getCustomerId()))
                .customerId(request.getCustomerId())
                .balance(request.getInitialBalance())
                .status(AccountStatus.ACTIVE)
                .type(request.getType())
                .build();

        // Save account
        Account saved = accountRepository.save(account);
        log.info("Account created with ID: {}", saved.getAccountId());

        // Create event
        AccountCreatedEvent event = new AccountCreatedEvent(
                saved.getAccountId(),
                saved.getCustomerId(),
                saved.getType(),
                saved.getBalance()
        );

        rabbitTemplate.convertAndSend(EXCHANGE, ACCOUNT_ROUTING_KEY, event);
        log.info("Published AccountCreatedEvent for account: {}", saved.getAccountId());

        return toResponse(saved);
    }

    @Override
    public CreateAccountResponse getAccount(Long accountId){
        return accountRepository.findById(accountId)
                .map(this::toResponse)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Override
    public List<CreateAccountResponse> getAccountsByCustomer(Long customerId){
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CreateAccountResponse updateAccountStatus(Long accountId, String status){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        account.setStatus(AccountStatus.valueOf(status.toUpperCase()));
        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId){
        if(!accountRepository.existsById(accountId)){
            throw new AccountNotFoundException(accountId);
        }
        accountRepository.deleteById(accountId);
        log.info("Deleted account: {}", accountId);
    }


    // ---- Helper Functions ----

    private Long generateAccountId(Long customerId){
        // Account ID should be: customerId{3 random digits}
        // If account exists loop again
        long accountId;
        do {
            long suffix = (long) (Math.random() * 900) + 100; // 100-999
            accountId = Long.parseLong(customerId + String.valueOf(suffix));
        } while (accountRepository.existsById(accountId));
        return accountId;
    }

    private CreateAccountResponse toResponse(Account a){
        return CreateAccountResponse.builder()
                .accountId(a.getAccountId())
                .customerId(a.getCustomerId())
                .balance(a.getBalance())
                .status(a.getStatus())
                .type(a.getType())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
