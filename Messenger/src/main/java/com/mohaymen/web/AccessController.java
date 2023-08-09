package com.mohaymen.web;

import com.mohaymen.service.AccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.Map;

@RestController
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }


    @ResponseBody
    @GetMapping("/access/login")
    public ResponseEntity<String> login(@RequestBody Map<String, Object> requestBody) {
        String email = (String) requestBody.get("email");
        byte[] password = ((String) requestBody.get("password")).getBytes();
        String ip = (String) requestBody.get("ip");
        try {
            String jwt = accessService.login(email, password, ip);
            return ResponseEntity.ok().body("{\"jwt\": \"" + jwt + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
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
}
