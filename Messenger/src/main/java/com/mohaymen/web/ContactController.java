package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.ContactService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService){
        this.contactService = contactService;
    }

    @PostMapping("/contacts")
    public String addContact(@RequestBody Map<String, Object> contactInfo){
        String customName = (String) contactInfo.get("customName");
        String username = (String) contactInfo.get("username");
        String token = (String) contactInfo.get("jwt");
        Long id = null;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
        } catch (Exception e){

        }
        if(contactService.addContact(id,username, customName))
            return "Successfully added";
        return "Failed";
    }

    @GetMapping("/contacts/{id}")
    public void getContacts(@PathVariable Long id){

    }
}
