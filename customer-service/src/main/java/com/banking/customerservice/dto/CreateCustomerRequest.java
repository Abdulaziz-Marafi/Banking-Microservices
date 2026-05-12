package com.banking.customerservice.dto;

import com.banking.customerservice.enums.CustomerType;
import jakarta.validation.constraints.*;
import lombok.Data;

// Request DTO for creating a customer (email & mobile not required)
@Data
public class CreateCustomerRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message ="Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Civil ID is required")
    private String civilId;

    @NotNull(message = "Customer Type is required")
    private CustomerType type;

    @NotBlank(message = "Address is required")
    private String address;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number")
    private String mobile;


}
