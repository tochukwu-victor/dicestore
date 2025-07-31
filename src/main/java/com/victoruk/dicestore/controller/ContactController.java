package com.victoruk.dicestore.controller;

import com.victoruk.dicestore.dto.ContactRequestDto;
import com.victoruk.dicestore.service.IContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/contacts")
//@RequiredArgsConstructor
public class ContactController {

    private final IContactService iContactService;

    public ContactController(IContactService iContactService) {
        this.iContactService = iContactService;
    }

    @PostMapping
    public ResponseEntity<String>  saveContact(@Valid @RequestBody ContactRequestDto contactRequestDto) {
        iContactService.saveContact(contactRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Request processed successfully");

    }



}