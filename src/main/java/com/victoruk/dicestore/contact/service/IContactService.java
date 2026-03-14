package com.victoruk.dicestore.contact.service;

import com.victoruk.dicestore.contact.dto.ContactRequestDto;
import com.victoruk.dicestore.contact.dto.ContactResponseDto;

import java.util.List;

public interface IContactService {

    boolean saveContact(ContactRequestDto contactRequestDto);

    List<ContactResponseDto> getAllOpenMessages();

    void updateMessageStatus(Long contactId, String status);
}
