package com.victoruk.dicestore.admin.controller;

import com.victoruk.dicestore.common.constants.ApplicationConstants;
import com.victoruk.dicestore.contact.dto.ContactResponseDto;
import com.victoruk.dicestore.common.response.ResponseDto;
import com.victoruk.dicestore.contact.service.IContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Messages", description = "Admin management of customer contact messages")
public class AdminContactController {

    private final IContactService iContactService;

    @GetMapping
    @Operation(summary = "Get all open messages",
            description = "Returns all unresolved contact messages submitted by customers")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Messages fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only")
    })
    public ResponseEntity<List<ContactResponseDto>> getAllOpenMessages() {
        log.info("Fetching all open contact messages");
        List<ContactResponseDto> messages = iContactService.getAllOpenMessages();
        log.info("Found {} open messages", messages.size());
        return ResponseEntity.ok(messages);
    }

    @PatchMapping("/{contactId}/close")
    @Operation(summary = "Close a contact message",
            description = "Marks a customer message as closed. Cannot be reopened.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Message closed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin only"),
            @ApiResponse(responseCode = "404", description = "Message not found")
    })
    public ResponseEntity<ResponseDto> closeMessage(@PathVariable Long contactId) {
        log.info("Closing contact message with id: {}", contactId);
        iContactService.updateMessageStatus(contactId, ApplicationConstants.CLOSED_MESSAGE);
        ResponseDto response = new ResponseDto("200", "Contact #" + contactId + " has been closed.");
        log.info("Message {} successfully closed", contactId);
        return ResponseEntity.ok(response);
    }
}