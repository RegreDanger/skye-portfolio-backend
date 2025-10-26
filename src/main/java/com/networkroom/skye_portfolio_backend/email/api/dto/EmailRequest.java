package com.networkroom.skye_portfolio_backend.email.api.dto;

public record EmailRequest(
    String targetEmail,
    String fromEmail
) {}
