package com.capgemini.adminservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryAddressDTO {
    private String fullName;
    private String phoneNumber;
    private String addressLine;
    private String city;
    private String state;
    private String pinCode;
}
