package com.networkroom.skye_portfolio_backend.email.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(

    @NotBlank
    String token

) {}
