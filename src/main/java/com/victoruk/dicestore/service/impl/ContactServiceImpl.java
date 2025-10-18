

package com.victoruk.dicestore.service.impl;

import com.victoruk.dicestore.constant.ApplicationConstants;
import com.victoruk.dicestore.dto.ContactResponseDto;
import com.victoruk.dicestore.entity.Contact;
import com.victoruk.dicestore.dto.ContactRequestDto;
import com.victoruk.dicestore.exception.ResourceNotFoundException;
import com.victoruk.dicestore.repository.ContactRepository;
import com.victoruk.dicestore.service.IContactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContactServiceImpl implements IContactService {

    private final ContactRepository contactRepository;

    public ContactServiceImpl(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @Override
    public boolean saveContact(ContactRequestDto contactRequestDto) {
        log.info("Saving new contact message from [{} - {}]", contactRequestDto.getName(), contactRequestDto.getEmail());
        Contact contact = transformToEntity(contactRequestDto);
        contactRepository.save(contact);
        log.info("Contact message [{}] saved successfully with status [{}]", contact.getId(), contact.getStatus());
        return true;
    }

    @Override
    public List<ContactResponseDto> getAllOpenMessages() {
        log.info("Fetching all contact messages with status [{}]", ApplicationConstants.OPEN_MESSAGE);
        List<Contact> contacts = contactRepository.findByStatus(ApplicationConstants.OPEN_MESSAGE);

        List<ContactResponseDto> responseList = contacts.stream()
                .map(this::mapToContactResponseDTO)
                .collect(Collectors.toList());

        log.info("Retrieved [{}] open contact messages", responseList.size());
        return responseList;
    }

    @Override
    public void updateMessageStatus(Long contactId, String status) {
        log.info("Updating status of contact message [{}] to [{}]", contactId, status);

        Contact contact = contactRepository.findById(contactId).orElseThrow(() -> {
            log.error("Contact with id [{}] not found", contactId);
            return new ResourceNotFoundException("Contact", "ContactID", contactId.toString());
        });

        contact.setStatus(status);
        contactRepository.save(contact);
        log.info("Contact message [{}] updated successfully to status [{}]", contactId, status);
    }

    private ContactResponseDto mapToContactResponseDTO(Contact contact) {
        return new ContactResponseDto(
                contact.getId(),
                contact.getName(),
                contact.getEmail(),
                contact.getMobileNumber(),
                contact.getMessage(),
                contact.getStatus()
        );
    }

    private Contact transformToEntity(ContactRequestDto contactRequestDto) {
        Contact contact = new Contact();
        BeanUtils.copyProperties(contactRequestDto, contact);
        return contact;
    }
}








//package com.victoruk.dicestore.service.impl;
//
//
//
//import com.victoruk.dicestore.constant.ApplicationConstants;
//import com.victoruk.dicestore.dto.ContactResponseDto;
//import com.victoruk.dicestore.entity.Contact;
//import com.victoruk.dicestore.dto.ContactRequestDto;
//import com.victoruk.dicestore.exception.ResourceNotFoundException;
//import com.victoruk.dicestore.repository.ContactRepository;
//import com.victoruk.dicestore.service.IContactService;
//import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
////    @RequiredArgsConstructor
//    public class ContactServiceImpl implements IContactService {
//
//        private final ContactRepository contactRepository;
//
//        public ContactServiceImpl(ContactRepository contactRepository) {
//            this.contactRepository = contactRepository;
//        }
//
//        @Override
//        public boolean saveContact(ContactRequestDto contactRequestDto) {
//
//                Contact contact = transformToEntity(contactRequestDto);
//                contactRepository.save(contact);
//                return true;
//        }
//
//
//    @Override
//    public List<ContactResponseDto> getAllOpenMessages() {
//        List<Contact> contacts = contactRepository.findByStatus(ApplicationConstants.OPEN_MESSAGE);
//        return contacts.stream().map(this::mapToContactResponseDTO).collect(Collectors.toList());
//    }
//
//    @Override
//    public void updateMessageStatus(Long contactId, String status) {
//        Contact contact = contactRepository.findById(contactId).orElseThrow(
//                () -> new ResourceNotFoundException("Contact", "ContactID", contactId.toString())
//        );
//        contact.setStatus(status);
//        contactRepository.save(contact);
//    }
//
//    private ContactResponseDto mapToContactResponseDTO(Contact contact) {
//        ContactResponseDto responseDTO = new ContactResponseDto(
//                contact.getId(),
//                contact.getName(),
//                contact.getEmail(),
//                contact.getMobileNumber(),
//                contact.getMessage(),
//                contact.getStatus()
//        );
//        return responseDTO;
//    }
//
//        private Contact transformToEntity(ContactRequestDto contactRequestDto) {
//            Contact contact = new Contact();
//            BeanUtils.copyProperties(contactRequestDto, contact);
//            return contact;
//        }
//
//}
