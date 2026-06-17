package com.victoruk.dicestore.contact.controller;

import com.victoruk.dicestore.contact.dto.ContactRequestDto;
import com.victoruk.dicestore.contact.service.IContactService;
import com.victoruk.dicestore.common.response.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Contact", description = "Customer contact and messaging endpoints")
public class ContactController {

    private final IContactService contactService;

    @PostMapping
    @Operation(
            summary = "Submit a contact message",
            description = "Allows customers to submit contact messages with their name, email, and message content. " +
                    "Messages are stored for admin review and follow-up."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Contact message submitted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    { "message": "Request processed successfully" }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed — missing or invalid fields (name, email, message required)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })

    public ResponseEntity<String> saveContact(@Valid @RequestBody ContactRequestDto contactRequestDto) {
        log.info("Received contact request from: {} <{}>",
                contactRequestDto.getName(), contactRequestDto.getEmail());

        contactService.saveContact(contactRequestDto);

        log.info("Contact request saved successfully for {}", contactRequestDto.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Request processed successfully");
    }







}
