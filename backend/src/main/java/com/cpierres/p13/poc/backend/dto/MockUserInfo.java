package com.cpierres.p13.poc.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockUserInfo {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}