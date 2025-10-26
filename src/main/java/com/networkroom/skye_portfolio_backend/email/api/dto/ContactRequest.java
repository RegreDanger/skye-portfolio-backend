package com.networkroom.skye_portfolio_backend.email.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactRequest (
    @Email
    @NotBlank
    String email,

    @NotBlank
    String username,

    @NotBlank
    String topic,

    @NotBlank
    String message
) {}

