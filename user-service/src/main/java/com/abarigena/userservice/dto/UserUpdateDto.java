package com.abarigena.userservice.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    private String phoneNumber;

    private String photoUrl;

    @Size(max = 1000)
    private String additionalInfo;
}
