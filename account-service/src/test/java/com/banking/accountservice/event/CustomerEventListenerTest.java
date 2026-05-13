package com.banking.accountservice.event;

import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.enums.AccountType;
import com.banking.accountservice.enums.CustomerType;
import com.banking.accountservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerEventListenerTest {

    @Mock private AccountService accountService;
    @InjectMocks private CustomerEventListener listener;

    @Test
    void handleCustomerCreated_autoCreatesSavingsAccountWithZeroBalance() {
        CustomerCreatedEvent event = new CustomerCreatedEvent(1234567L, "John Doe", CustomerType.RETAIL);

        listener.handleCustomerCreated(event);

        ArgumentCaptor<CreateAccountRequest> captor = ArgumentCaptor.forClass(CreateAccountRequest.class);
        verify(accountService).createAccount(captor.capture());

        CreateAccountRequest captured = captor.getValue();
        assertThat(captured.getCustomerId()).isEqualTo(1234567L);
        assertThat(captured.getType()).isEqualTo(AccountType.SAVINGS);
        assertThat(captured.getInitialBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
