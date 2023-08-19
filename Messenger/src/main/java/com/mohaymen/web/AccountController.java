package com.mohaymen.web;


import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.LoginInfo;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @JsonView(Views.ProfileLoginInfo.class)
    @GetMapping("/lastSeen")
    public String getLastSeen(@RequestBody Map<String, Object> requestBody){
        String userId=(String)requestBody.get("userId");
        try {
            return accountService.getLastSeen(Long.valueOf(userId));
        } catch (Exception e) {
           return e.getMessage();
        }
    }
}
