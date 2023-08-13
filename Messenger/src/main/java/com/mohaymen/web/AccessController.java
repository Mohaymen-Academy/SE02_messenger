package com.mohaymen.web;

import com.mohaymen.model.Account;
import com.mohaymen.model.Profile;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.AccessService;
import jakarta.mail.MessagingException;
import org.apache.log4j.Logger;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CrossOrigin
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
    @PostMapping("/login")
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
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("ایمل یا رمز عبور اشتباه است");
        }
    }

    @GetMapping("/signup")
    public String isValidSignUpInfo(@RequestParam(name = "email") String email) {
        if (accessService.infoValidation(email)) {
            logger.info("Successful Signup Validation : " + email);
            return "success";
        }
        logger.info("Failed Signup Validation : email exits");
        return "fail";
    }

    @PostMapping("/signup")
    public String signup(@RequestBody Map<String, Object> requestBody){
        String name;
        String email;
        try {
            name = (String) requestBody.get("name");
            email = (String) requestBody.get("email");
        } catch (Exception e) {
            logger.info("Failed signup: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            if (accessService.signup(name, email)) {
                logger.info("Successful signup: name = " + name + ", email = " + email);
                return "success";
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        logger.info("Failed signup: information is not valid");
        return "fail";
    }

    @PostMapping("/verify")
    public String verify(@RequestBody Map<String, Object> requestBody){
        String name;
        String email;
        byte[] password;
        String inputCode;
        try {
            name = (String) requestBody.get("name");
            email = (String) requestBody.get("email");
            password = ((String) requestBody.get("password")).getBytes();
            inputCode = (String) requestBody.get("code");
        } catch (Exception e) {
//            logger.info("Failed signup: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            if (accessService.verify(name, email, password, inputCode)) {
                logger.info("Successful verify: name = " + name + ", email = " + email);
                return "success";
            }
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE);
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
