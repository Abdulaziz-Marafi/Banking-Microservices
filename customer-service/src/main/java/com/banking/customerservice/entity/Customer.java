package com.banking.customerservice.entity;

import com.banking.customerservice.enums.CustomerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name ="customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(nullable = false)
    private String name;

    @Column(name ="civil_id", nullable = false, unique = true)
    private String civilId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type;

    @Column(nullable = false)
    private String address;

    private String email;

    private String mobile;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
