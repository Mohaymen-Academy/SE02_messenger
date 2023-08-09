package com.mohaymen.web;

import com.mohaymen.service.AccessService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/access/signup")
    public String isValidSignUpInfo(@RequestBody Map<String, Object> signupInfo) {
        if (accessService.infoValidation((String) signupInfo.get("email")))
            return "is valid";
        return "is not valid";
    }

    @PostMapping("/access/signup")
    public String signUp(@RequestBody Map<String, Object> signupInfo) {
        String name = (String) signupInfo.get("name");
        String email = (String) signupInfo.get("email");
        String password = (String) signupInfo.get("password");
        if (accessService.signUp(name, email, password.getBytes()))
            return "successful";
        return "fail";
    }
//
//    @GetMapping("/access/login")
//    public String login(@RequestParam(name = "username") String username,
//                        @RequestParam(name = "password") byte[] password) {
//        if (accessService.logIn(username, password))
//            return "successful login";
//        return "fail login";
//    }

}
