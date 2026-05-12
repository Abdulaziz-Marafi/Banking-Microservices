package com.banking.customerservice.repository;

import com.banking.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository  extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCivilId(String civilID);
    boolean existsByCivilId(String civilID);
}
