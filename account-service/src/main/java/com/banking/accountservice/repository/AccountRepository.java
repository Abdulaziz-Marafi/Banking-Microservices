package com.banking.accountservice.repository;

import com.banking.accountservice.entity.Account;
import com.banking.accountservice.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByCustomerId(Long customerId);

    long countByCustomerId(Long customerId);

    // To check if a customer has a specific account type, used to enforce 1 salary rule
    boolean existsByCustomerIdAndType(Long customerId, AccountType type);
}
