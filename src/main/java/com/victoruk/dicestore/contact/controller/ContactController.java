package com.victoruk.dicestore.contact.controller;

import com.victoruk.dicestore.contact.dto.ContactRequestDto;
import com.victoruk.dicestore.contact.service.IContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final IContactService contactService;

    @PostMapping
    public ResponseEntity<String> saveContact(@Valid @RequestBody ContactRequestDto contactRequestDto) {
        log.info("📩 Received contact request from: {} <{}>",
                contactRequestDto.getName(), contactRequestDto.getEmail());

        contactService.saveContact(contactRequestDto);

        log.info("✅ Contact request saved successfully for {}", contactRequestDto.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Request processed successfully");
    }
}
