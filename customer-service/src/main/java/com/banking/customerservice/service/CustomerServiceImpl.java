package com.banking.customerservice.service;

import com.banking.customerservice.dto.CreateCustomerRequest;
import com.banking.customerservice.dto.CreateCustomerResponse;
import com.banking.customerservice.entity.Customer;
import com.banking.customerservice.event.CustomerCreatedEvent;
import com.banking.customerservice.exception.CustomerNotFoundException;
import com.banking.customerservice.exception.DuplicateCustomerException;
import com.banking.customerservice.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService{
    private final CustomerRepository customerRepository;
    private final RabbitTemplate rabbitTemplate;

    // Rabbit MQ related fields - config
    private static final String EXCHANGE = "banking.exchange";
    private static final String CUSTOMER_ROUTING_KEY = "customer.created";

    @Override
    @Transactional
    public CreateCustomerResponse createCustomer(CreateCustomerRequest request){

        log.info("Creating customer with civil ID: {}", request.getCivilId());

        // Check if customer exists - throw already exists e if they do.
        if (customerRepository.existsByCivilId(request.getCivilId())){
            throw new DuplicateCustomerException(request.getCivilId());
        }

        // Create customer since they don't exist
        Customer customer =  Customer.builder()
                .customerId(generateCustomerId())
                .name(request.getName())
                .civilId(request.getCivilId())
                .type(request.getType())
                .address(request.getAddress())
                .email(request.getEmail())
                .mobile(request.getMobile())
                .build();

        // Save cutomer to db
        Customer saved = customerRepository.save(customer);
        log.info("Customer created with ID: {}", saved.getCustomerId());

        CustomerCreatedEvent event = new CustomerCreatedEvent(
                saved.getCustomerId(),
                saved.getName(),
                saved.getType()  // maps to customerType field via @AllArgsConstructor order
        );

        rabbitTemplate.convertAndSend(EXCHANGE, CUSTOMER_ROUTING_KEY, event);
        log.info("Published CustomerCreatedEvent for customer: {}", saved.getCustomerId());

        return toResponse(saved);

    }

    @Override
    public CreateCustomerResponse getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .map(this::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Override
    public List<CreateCustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
    @Override
    @Transactional
    public CreateCustomerResponse updateCustomer(Long customerId, CreateCustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        customer.setName(request.getName());
        customer.setAddress(request.getAddress());
        customer.setEmail(request.getEmail());
        customer.setMobile(request.getMobile());
        customer.setType(request.getType());

        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public void deleteCustomer(Long customerId){
        // Check if customer exists and then delete
        if(!customerRepository.existsById(customerId)){
            throw new CustomerNotFoundException(customerId);
        }
        customerRepository.deleteById(customerId);
        log.info("Deleted customer: {}", customerId);

    }

    private Long generateCustomerId() {
        Long id;
        do {
            id = ThreadLocalRandom.current().nextLong(1_000_000L, 10_000_000L);
        } while (customerRepository.existsById(id));
        return id;
    }

    private CreateCustomerResponse toResponse(Customer c) {
        return CreateCustomerResponse.builder()
                .customerId(c.getCustomerId())
                .name(c.getName())
                .civilId(c.getCivilId())
                .type(c.getType())
                .address(c.getAddress())
                .email(c.getEmail())
                .phone(c.getMobile())
                .createdAt(c.getCreatedAt())
                .build();
    }

}
