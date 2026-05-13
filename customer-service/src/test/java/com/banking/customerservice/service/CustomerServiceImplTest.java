package com.banking.customerservice.service;

import com.banking.customerservice.dto.CreateCustomerRequest;
import com.banking.customerservice.dto.CreateCustomerResponse;
import com.banking.customerservice.entity.Customer;
import com.banking.customerservice.enums.CustomerType;
import com.banking.customerservice.event.CustomerCreatedEvent;
import com.banking.customerservice.exception.CustomerNotFoundException;
import com.banking.customerservice.exception.DuplicateCustomerException;
import com.banking.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks private CustomerServiceImpl customerService;

    private Customer customer;
    private CreateCustomerRequest request;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .customerId(1234567L)
                .name("John Doe")
                .civilId("CIV001")
                .type(CustomerType.RETAIL)
                .address("123 Main St")
                .email("john@example.com")
                .mobile("+96512345678")
                .createdAt(LocalDateTime.now())
                .build();

        request = new CreateCustomerRequest();
        request.setName("John Doe");
        request.setCivilId("CIV001");
        request.setType(CustomerType.RETAIL);
        request.setAddress("123 Main St");
        request.setEmail("john@example.com");
        request.setMobile("+96512345678");
    }

    // --- createCustomer ---

    @Test
    void createCustomer_success_returnsResponseAndPublishesEvent() {
        when(customerRepository.existsByCivilId("CIV001")).thenReturn(false);
        when(customerRepository.existsById(anyLong())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CreateCustomerResponse response = customerService.createCustomer(request);

        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getCivilId()).isEqualTo("CIV001");
        assertThat(response.getType()).isEqualTo(CustomerType.RETAIL);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), (Object) any(CustomerCreatedEvent.class));
    }

    @Test
    void createCustomer_duplicateCivilId_throwsDuplicateCustomerException() {
        when(customerRepository.existsByCivilId("CIV001")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessageContaining("CIV001");

        verify(customerRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    // --- getCustomer ---

    @Test
    void getCustomer_found_returnsResponse() {
        when(customerRepository.findById(1234567L)).thenReturn(Optional.of(customer));

        CreateCustomerResponse response = customerService.getCustomer(1234567L);

        assertThat(response.getCustomerId()).isEqualTo(1234567L);
        assertThat(response.getName()).isEqualTo("John Doe");
    }

    @Test
    void getCustomer_notFound_throwsCustomerNotFoundException() {
        when(customerRepository.findById(9999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomer(9999999L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("9999999");
    }

    // --- getAllCustomers ---

    @Test
    void getAllCustomers_returnsAllMappedCustomers() {
        Customer second = Customer.builder()
                .customerId(2345678L).name("Jane Doe").civilId("CIV002")
                .type(CustomerType.CORPORATE).address("456 Side St").build();

        when(customerRepository.findAll()).thenReturn(List.of(customer, second));

        List<CreateCustomerResponse> responses = customerService.getAllCustomers();

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(CreateCustomerResponse::getName)
                .containsExactly("John Doe", "Jane Doe");
    }

    @Test
    void getAllCustomers_empty_returnsEmptyList() {
        when(customerRepository.findAll()).thenReturn(List.of());

        assertThat(customerService.getAllCustomers()).isEmpty();
    }

    // --- updateCustomer ---

    @Test
    void updateCustomer_found_updatesAndReturnsResponse() {
        request.setName("John Updated");
        request.setAddress("999 New St");
        when(customerRepository.findById(1234567L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CreateCustomerResponse response = customerService.updateCustomer(1234567L, request);

        assertThat(response).isNotNull();
        verify(customerRepository).save(customer);
    }

    @Test
    void updateCustomer_notFound_throwsCustomerNotFoundException() {
        when(customerRepository.findById(9999999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(9999999L, request))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepository, never()).save(any());
    }

    // --- deleteCustomer ---

    @Test
    void deleteCustomer_found_deletesSuccessfully() {
        when(customerRepository.existsById(1234567L)).thenReturn(true);

        customerService.deleteCustomer(1234567L);

        verify(customerRepository).deleteById(1234567L);
    }

    @Test
    void deleteCustomer_notFound_throwsCustomerNotFoundException() {
        when(customerRepository.existsById(9999999L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomer(9999999L))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(customerRepository, never()).deleteById(any());
    }
}
