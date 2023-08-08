package com.mohaymen.web;

import com.mohaymen.service.AccessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        byte[] password = (byte[]) requestBody.get("password");
        String ip = (String) requestBody.get("ip");
        try {
            String jwt = accessService.login(email, password, ip);
            return ResponseEntity.ok().body("{\"message\": \"" + jwt + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/access/signup")
    public String isValidSignUpInfo(@RequestParam(name = "username") String username,
                                    @RequestParam(name = "email") String email) {
        if (accessService.infoValidation(username, email))
            return "is valid";
        return "is not valid";
    }

    @PostMapping("/access/signup")
    public String signUp(@RequestParam(name = "name") String username,
                         @RequestParam(name = "password") byte[] password,
                         @RequestParam(name = "email") String email) {
        if (accessService.signUp(username, email, password))
            return "successful";
        return "fail";
    }
}
