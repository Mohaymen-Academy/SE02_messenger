package com.mohaymen.service;

import com.mohaymen.model.Profile;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ProfileRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import com.mohaymen.noName.salt;
import org.yaml.snakeyaml.util.ArrayUtils;

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
//    public byte[] createSalt(byte[]password){
//        byte[]saltArray= salt.getSaltArray();
//        return saltArray;
//    }
    @SneakyThrows
    public byte[] configPassword(byte[] password) {
        byte[] saltArray = salt.getSaltArray();
        //todo add saltArray to the AccountTable kimia add kon

        byte[] combined = new byte[password.length + saltArray.length];

        System.arraycopy(password, 0, combined, 0, password.length);
        System.arraycopy(saltArray, 0, combined, password.length, saltArray.length);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        combined = messageDigest.digest(combined);
        return combined;
    }

}

