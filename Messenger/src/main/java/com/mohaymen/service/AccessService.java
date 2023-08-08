package com.mohaymen.service;

import com.mohaymen.model.Profile;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

@Service
public class AccessService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    public AccessService(AccountRepository accountRepository, ProfileRepository profileRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    public Boolean infoValidation(String username, String email) {
        Optional<Profile> profile = profileRepository.findById(1L);
        System.out.println(profile.get().getHandle());
        return true;
//        if( != null)
//            return false;
//        for (User u : db.values()){
//            if(u.getEmail().equals(email))
//                return false;
//        }
    }

//    public Boolean signUp(String username, String email, byte[] password) {
//        if(!infoValidation(username, email))
//            return false;
//        User u = new User();
//        u.setUsername(username);
//        u.setEmail(email);
//        u.setPassword(password);
//        db.put(username, u);
//        return true;
//    }
//
//    public Boolean logIn(String username, byte[] password) {
//        if(!db.containsKey(username))
//            return false;
//        return Arrays.equals(db.get(username).getPassword(), password);
//    }

}

