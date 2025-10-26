package com.networkroom.skye_portfolio_backend.email.api.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.networkroom.skye_portfolio_backend.email.api.dto.ContactRequest;
import com.networkroom.skye_portfolio_backend.email.app.MailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContactController {
    
    private final MailService mailService;

    @PostMapping("/contact")
    public Mono<ResponseEntity<?>> sendContact(@Valid @RequestBody Mono<ContactRequest> requestContact) {
        return requestContact.flatMap(req -> mailService.sendMessage(req))
            .map(res -> {
                    return res ? ResponseEntity.status(200).body(Map.of("successful", "email sent!")) :
                           ResponseEntity.status(500).body(Map.of("error", "there was an error sending the email"));
                });
    }
}
