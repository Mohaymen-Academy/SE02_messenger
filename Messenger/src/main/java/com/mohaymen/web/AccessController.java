package com.mohaymen.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessController {
    

    @GetMapping("/access/signup-validation")
    public String isValidSignUpInfo(@RequestPart("username") String username,@RequestPart("email") String email) {
        return "OK";
    }

    @GetMapping("/access/signup")
    public String signUp(@RequestPart("username") String username,
                         @RequestPart("password") byte[] password,
                         @RequestPart("email") String email) {
        return "OK";
    }


    @GetMapping("/access/login")
    public String login(@RequestPart("username") String username,
                         @RequestPart("password") byte[] password) {
        return "OK";
    }


}
