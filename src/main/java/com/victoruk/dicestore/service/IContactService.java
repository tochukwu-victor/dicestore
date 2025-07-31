package com.victoruk.dicestore.service;

import com.victoruk.dicestore.dto.ContactRequestDto;
import com.victoruk.dicestore.dto.ContactResponseDto;

import java.util.List;

public interface IContactService {

    boolean saveContact(ContactRequestDto contactRequestDto);

    List<ContactResponseDto> getAllOpenMessages();

    void updateMessageStatus(Long contactId, String status);
}
