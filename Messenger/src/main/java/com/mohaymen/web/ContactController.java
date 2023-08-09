package com.mohaymen.web;

import com.mohaymen.model.ProfileDisplay;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService){
        this.contactService = contactService;
    }

    @PostMapping("/contacts")
    public ProfileDisplay addContact(@RequestBody Map<String, Object> contactInfo){
        String customName = (String) contactInfo.get("customName");
        String username = (String) contactInfo.get("username");
        String token = (String) contactInfo.get("jwt");
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ProfileDisplay profileDisplay = contactService.addContact(id,username, customName);
        if(profileDisplay == null)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        return profileDisplay;
    }

    @GetMapping("/contacts")
    public List<ProfileDisplay> getContacts(@RequestBody Map<String, Object> currentJwt){
        String jwt = (String) currentJwt.get("jwt");
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(jwt);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return contactService.getContactsOfOneUser(id);
    }
}
