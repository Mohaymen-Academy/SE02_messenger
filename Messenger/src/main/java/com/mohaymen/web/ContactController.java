package com.mohaymen.web;

import com.mohaymen.service.ContactService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService){
        this.contactService = contactService;
    }

    @PostMapping("/contacts/{id}")
    public String addContact(@PathVariable Long id, @RequestBody Map<String, Object> contactInfo){
        String customName = (String) contactInfo.get("customName");
        String username = (String) contactInfo.get("username");
        if(contactService.addContact(id,username, customName))
            return "Successfully added";
        return "Failed";
    }

    @GetMapping("/contacts/{id}")
    public void getContacts(@PathVariable Long id){

    }
}
