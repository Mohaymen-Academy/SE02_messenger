package com.mohaymen.web;

import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.AccessService;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/access")
public class AccessController {

    private final AccessService accessService;

    private final Logger logger;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
        logger = Logger.getLogger(AccessController.class);
    }

    @ResponseBody
    @GetMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, Object> requestBody) {
        String email;
        byte[] password;
        try {
            email = (String) requestBody.get("email");
            password = ((String) requestBody.get("password")).getBytes();
        } catch (Exception e) {
            logger.info("Failed Login: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            String jwt = accessService.login(email, password);
            logger.info("Successful Login: " + email);
            return ResponseEntity.ok().body("{\"jwt\": \"" + jwt + "\"}");
        } catch (Exception e) {
            logger.info("Failed Login: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @GetMapping("/signup")
    public String isValidSignUpInfo(@RequestBody Map<String, Object> requestBody) {
        String email;
        try {
            email = (String) requestBody.get("email");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (accessService.infoValidation(email))
            return "is valid";
        return "is not valid";
    }

    @PostMapping("/signup")
    public String signup(@RequestBody Map<String, Object> requestBody) {
        String name;
        String email;
        byte[] password;
        try {
            name = (String) requestBody.get("name");
            email = (String) requestBody.get("email");
            password = ((String) requestBody.get("password")).getBytes();
        } catch (Exception e) {
            logger.info("Failed signup: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (accessService.signup(name, email, password)) {
            logger.info("Successful signup: name = " + name + ", email = " + email);
            return "successful";
        }
        logger.info("Failed signup: information is not valid");
        return "fail";
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(@RequestBody Map<String, Object> accountInfo){
        String token = (String) accountInfo.get("jwt");
        byte[] password =((String) accountInfo.get("password")).getBytes();
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
            accessService.deleteAccount(id, password);
        } catch (Exception e){
            logger.info("Failed delete account" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().body("successful");
    }
}
