package com.banking.accountservice.service;

import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.dto.CreateAccountResponse;
import com.banking.accountservice.dto.TransactionRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.enums.AccountStatus;
import com.banking.accountservice.enums.AccountType;
import com.banking.accountservice.event.AccountCreatedEvent;
import com.banking.accountservice.exception.AccountNotActiveException;
import com.banking.accountservice.exception.AccountNotFoundException;
import com.banking.accountservice.exception.InsufficientFundsException;
import com.banking.accountservice.exception.MaxAccountsReachedException;
import com.banking.accountservice.exception.SalaryAccountExistsException;
import com.banking.accountservice.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private AccountServiceImpl accountService;

    private Account account;
    private CreateAccountRequest request;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .accountId(1234567100L)
                .customerId(1234567L)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .type(AccountType.SAVINGS)
                .createdAt(LocalDateTime.now())
                .build();

        request = new CreateAccountRequest();
        request.setCustomerId(1234567L);
        request.setType(AccountType.SAVINGS);
        request.setInitialBalance(BigDecimal.ZERO);
    }

    // --- createAccount ---

    @Test
    void createAccount_success_returnsResponseAndPublishesEvent() {
        when(accountRepository.countByCustomerId(1234567L)).thenReturn(0L);
        when(accountRepository.existsById(anyLong())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        CreateAccountResponse response = accountService.createAccount(request);

        assertThat(response.getCustomerId()).isEqualTo(1234567L);
        assertThat(response.getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any(AccountCreatedEvent.class));
    }

    @Test
    void createAccount_maxAccountsReached_throwsMaxAccountsReachedException() {
        when(accountRepository.countByCustomerId(1234567L)).thenReturn(10L);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(MaxAccountsReachedException.class)
                .hasMessageContaining("1234567");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_salaryAlreadyExists_throwsSalaryAccountExistsException() {
        request.setType(AccountType.SALARY);
        when(accountRepository.countByCustomerId(1234567L)).thenReturn(1L);
        when(accountRepository.existsByCustomerIdAndType(1234567L, AccountType.SALARY)).thenReturn(true);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(SalaryAccountExistsException.class)
                .hasMessageContaining("1234567");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_salaryType_noExistingSalary_createsSuccessfully() {
        request.setType(AccountType.SALARY);
        Account salaryAccount = Account.builder()
                .accountId(1234567101L).customerId(1234567L)
                .balance(BigDecimal.ZERO).status(AccountStatus.ACTIVE).type(AccountType.SALARY).build();

        when(accountRepository.countByCustomerId(1234567L)).thenReturn(1L);
        when(accountRepository.existsByCustomerIdAndType(1234567L, AccountType.SALARY)).thenReturn(false);
        when(accountRepository.existsById(anyLong())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(salaryAccount);

        CreateAccountResponse response = accountService.createAccount(request);

        assertThat(response.getType()).isEqualTo(AccountType.SALARY);
    }

    // --- getAccount ---

    @Test
    void getAccount_found_returnsResponse() {
        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));

        CreateAccountResponse response = accountService.getAccount(1234567100L);

        assertThat(response.getAccountId()).isEqualTo(1234567100L);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getAccount_notFound_throwsAccountNotFoundException() {
        when(accountRepository.findById(9999999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(9999999999L))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("9999999999");
    }

    // --- getAccountsByCustomer ---

    @Test
    void getAccountsByCustomer_returnsAllAccounts() {
        Account second = Account.builder()
                .accountId(1234567101L).customerId(1234567L)
                .balance(new BigDecimal("500.000")).status(AccountStatus.ACTIVE).type(AccountType.INVESTMENT).build();

        when(accountRepository.findByCustomerId(1234567L)).thenReturn(List.of(account, second));

        List<CreateAccountResponse> responses = accountService.getAccountsByCustomer(1234567L);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CreateAccountResponse::getType)
                .containsExactlyInAnyOrder(AccountType.SAVINGS, AccountType.INVESTMENT);
    }

    @Test
    void getAccountsByCustomer_noAccounts_returnsEmptyList() {
        when(accountRepository.findByCustomerId(1234567L)).thenReturn(List.of());

        assertThat(accountService.getAccountsByCustomer(1234567L)).isEmpty();
    }

    // --- updateAccountStatus ---

    @Test
    void updateAccountStatus_found_updatesStatus() {
        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        CreateAccountResponse response = accountService.updateAccountStatus(1234567100L, "FROZEN");

        verify(accountRepository).save(account);
        assertThat(response).isNotNull();
    }

    @Test
    void updateAccountStatus_notFound_throwsAccountNotFoundException() {
        when(accountRepository.findById(9999999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.updateAccountStatus(9999999999L, "CLOSED"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    // --- deposit ---

    @Test
    void deposit_success_addsAmountToBalance() {
        account.setBalance(new BigDecimal("100.000"));
        TransactionRequest tx = new TransactionRequest();
        tx.setAmount(new BigDecimal("50.000"));

        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        CreateAccountResponse response = accountService.deposit(1234567100L, tx);

        assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("150.000"));
    }

    @Test
    void deposit_accountNotFound_throwsAccountNotFoundException() {
        when(accountRepository.findById(9999999999L)).thenReturn(Optional.empty());

        TransactionRequest tx = new TransactionRequest();
        tx.setAmount(new BigDecimal("50.000"));

        assertThatThrownBy(() -> accountService.deposit(9999999999L, tx))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void deposit_accountNotActive_throwsAccountNotActiveException() {
        account.setStatus(AccountStatus.FROZEN);
        TransactionRequest tx = new TransactionRequest();
        tx.setAmount(new BigDecimal("50.000"));

        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deposit(1234567100L, tx))
                .isInstanceOf(AccountNotActiveException.class);

        verify(accountRepository, never()).save(any());
    }

    // --- withdraw ---

    @Test
    void withdraw_success_subtractsAmountFromBalance() {
        account.setBalance(new BigDecimal("200.000"));
        TransactionRequest tx = new TransactionRequest();
        tx.setAmount(new BigDecimal("75.000"));

        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        CreateAccountResponse response = accountService.withdraw(1234567100L, tx);

        assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("125.000"));
    }

    @Test
    void withdraw_insufficientFunds_throwsInsufficientFundsException() {
        account.setBalance(new BigDecimal("10.000"));
        TransactionRequest tx = new TransactionRequest();
        tx.setAmount(new BigDecimal("500.000"));

        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(1234567100L, tx))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("insufficient funds");

        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_accountNotActive_throwsAccountNotActiveException() {
        account.setStatus(AccountStatus.CLOSED);
        TransactionRequest tx = new TransactionRequest();
        tx.setAmount(new BigDecimal("50.000"));

        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(1234567100L, tx))
                .isInstanceOf(AccountNotActiveException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void withdraw_exactBalance_succeeds() {
        account.setBalance(new BigDecimal("100.000"));
        TransactionRequest tx = new TransactionRequest();
        tx.setAmount(new BigDecimal("100.000"));

        when(accountRepository.findById(1234567100L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        CreateAccountResponse response = accountService.withdraw(1234567100L, tx);

        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- deleteAccount ---

    @Test
    void deleteAccount_found_deletesSuccessfully() {
        when(accountRepository.existsById(1234567100L)).thenReturn(true);

        accountService.deleteAccount(1234567100L);

        verify(accountRepository).deleteById(1234567100L);
    }

    @Test
    void deleteAccount_notFound_throwsAccountNotFoundException() {
        when(accountRepository.existsById(9999999999L)).thenReturn(false);

        assertThatThrownBy(() -> accountService.deleteAccount(9999999999L))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).deleteById(any());
    }
}
