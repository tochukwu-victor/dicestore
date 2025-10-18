package com.victoruk.dicestore.controller.admin;


import com.victoruk.dicestore.constant.ApplicationConstants;
import com.victoruk.dicestore.dto.ContactResponseDto;
import com.victoruk.dicestore.dto.ResponseDto;
import com.victoruk.dicestore.service.IContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
@Slf4j
public class AdminContactController {

    private final IContactService iContactService;

    // ✅ Get all open messages
    @GetMapping
    public ResponseEntity<List<ContactResponseDto>> getAllOpenMessages() {
        log.info("Fetching all open contact messages");
        List<ContactResponseDto> messages = iContactService.getAllOpenMessages();
        log.info("Found {} open messages", messages.size());
        return ResponseEntity.ok(messages);
    }

    // ✅ Close a message
    @PatchMapping("/{contactId}/close")
    public ResponseEntity<ResponseDto> closeMessage(@PathVariable Long contactId) {
        log.info("Closing contact message with id: {}", contactId);
        iContactService.updateMessageStatus(contactId, ApplicationConstants.CLOSED_MESSAGE);
        ResponseDto response = new ResponseDto("200", "Contact #" + contactId + " has been closed.");
        log.info("Message {} successfully closed", contactId);
        return ResponseEntity.ok(response);
    }


}
