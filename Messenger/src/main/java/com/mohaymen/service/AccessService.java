package com.mohaymen.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashMap;

@Service
public class AccessService {

    private final HashMap<String, User> db;

    public AccessService() {
        db = new HashMap<>();
    }

    public Boolean infoValidation(String username, String email) {
        if(db.containsKey(username))
            return false;
        for (User u : db.values()){
            if(u.getEmail().equals(email))
                return false;
        }
        return true;
    }

    public Boolean signUp(String username, String email, byte[] password) {
        if(!infoValidation(username, email))
            return false;
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(password);
        db.put(username, u);
        return true;
    }

    public Boolean logIn(String username, byte[] password) {
        if(!db.containsKey(username))
            return false;
        return Arrays.equals(db.get(username).getPassword(), password);
    }

}

@Getter
@Setter
class User {

    private String username;
    private String email;
    private byte[] password;

}