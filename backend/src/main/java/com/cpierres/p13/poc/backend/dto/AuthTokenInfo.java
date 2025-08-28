package com.cpierres.p13.poc.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthTokenInfo {
    private UUID userId;
    private String email;
    private String role;
    private boolean valid;
}