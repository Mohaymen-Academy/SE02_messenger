package com.mohaymen.web;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.LoginInfo;
import com.mohaymen.model.json_item.Views;
import com.mohaymen.repository.LogRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.service.AccessService;
import com.mohaymen.service.LogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/access")
public class AccessController {

    private final AccessService accessService;

    private final LogService logger;

    public AccessController(AccessService accessService, LogRepository logRepository) {
        this.accessService = accessService;
        this.logger = new LogService(logRepository, AccessController.class.getName());
    }

    @JsonView(Views.ProfileLoginInfo.class)
    @PostMapping("/login")
    public ResponseEntity<LoginInfo> login(@RequestBody Map<String, Object> requestBody) {
        String email;
        byte[] password;
        try {
            email = (String) requestBody.get("email");
            password = ((String) requestBody.get("password")).getBytes();
        } catch (Exception e) {
            logger.info("Failed login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginInfo("fail"));
        }
        try {
            LoginInfo loginInfo = accessService.login(email, password);
            logger.info("Successful login: " + email);
            return ResponseEntity.ok().body(loginInfo);
        } catch (Exception e) {
            logger.info("Failed login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new LoginInfo("ایمل یا رمز عبور اشتباه است"));
        }
    }

    @GetMapping("/signup")
    public ResponseEntity<String> isValidSignUpInfo(@RequestParam(name = "email") String email) {
        if (accessService.infoValidation(email)) {
            logger.info("Successful Signup Validation : " + email);
            return ResponseEntity.ok().body("success");
        }
        logger.info("Failed Signup Validation : email exits");
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("fail");
    }

    @JsonView(Views.ProfileLoginInfo.class)
    @PostMapping("/signup")
    public ResponseEntity<LoginInfo> signup(@RequestBody Map<String, Object> requestBody) {
        String name;
        String email;
        byte[] password;
        try {
            name = (String) requestBody.get("name");
            email = (String) requestBody.get("email");
            password = ((String) requestBody.get("password")).getBytes();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            logger.info("Failed signup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginInfo("fail"));
        }
        try {
            LoginInfo loginInfo = accessService.signup(name, email, password);
            logger.info("Successful signup: name = " + name + ", email = " + email);
            return ResponseEntity.ok().body(loginInfo);
        } catch (Exception e) {
            logger.info("Failed signup: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(new LoginInfo("fail"));
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<String> deleteAccount(@RequestHeader(name = "Authorization") String token,
                                                @RequestBody Map<String, Object> accountInfo) {
        System.out.println(11);
        byte[] password = ((String) accountInfo.get("password")).getBytes();
        Long id;
        try {
            id = JwtHandler.getIdFromAccessToken(token);
            accessService.deleteAccount(id, password);
        } catch (Exception e) {
            logger.info("Failed delete account " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok().body("successful");
    }


}
