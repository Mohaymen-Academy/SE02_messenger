package com.mohaymen.web;

import com.mohaymen.service.AccessService;
import org.springframework.web.bind.annotation.*;

@RestController
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/access/signup")
    public String isValidSignUpInfo(@RequestParam(name = "username") String username,
                                    @RequestParam(name = "email") String email) {
        if (accessService.infoValidation(username, email))
            return "is valid";
        return "is not valid";
    }

    @PostMapping("/access/signup")
    public String signUp(@RequestParam(name = "username") String username,
                         @RequestParam(name = "password") byte[] password,
                         @RequestParam(name = "email") String email) {
        if (accessService.signUp(username, email, password))
            return "successful";
        return "fail";
    }

    @GetMapping("/access/login")
    public String login(@RequestParam(name = "username") String username,
                        @RequestParam(name = "password") byte[] password) {
        if (accessService.logIn(username, password))
            return "successful login";
        return "fail login";
    }

}
