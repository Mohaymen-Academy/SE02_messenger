package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.ChatDisplay;
import com.mohaymen.model.Profile;
import com.mohaymen.model.Views;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ContactService;
import org.springframework.http.HttpStatus;
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

    @JsonView(Views.ChatDisplay.class)
    @PostMapping("/contacts")
    public Profile addContact(@RequestBody Map<String, Object> contactInfo){
        String customName = (String) contactInfo.get("customName");
        String username = (String) contactInfo.get("username");
        String token = (String) contactInfo.get("jwt");
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Profile profileDisplay = contactService.addContact(id,username, customName);
        if(profileDisplay == null)
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        return profileDisplay;
    }

    @JsonView(Views.ChatDisplay.class)
    @GetMapping("/contacts")
    public List<Profile> getContacts(@RequestBody Map<String, Object> currentJwt){
        String jwt = (String) currentJwt.get("jwt");
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(jwt);
        } catch (Exception e){
            System.out.println("hi");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return contactService.getContactsOfOneUser(id);
    }
}
