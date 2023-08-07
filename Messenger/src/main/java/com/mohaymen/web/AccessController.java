package com.mohaymen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessController {

    @GetMapping("/access/signup-validation")
    public String isValidSignUpInfo(String username, String email) {
        return "OK";
    }

    @GetMapping("access/signup")
    public String signUp(String username, byte[] password, String email) {
        return "OK";
    }


    @GetMapping("access/login")
    public String login(String username, byte[] password) {
        return "OK";
    }


}
