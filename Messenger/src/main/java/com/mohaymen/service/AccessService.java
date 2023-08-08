package com.mohaymen.service;

import com.mohaymen.model.Account;
import com.mohaymen.model.ChatType;
import com.mohaymen.model.Profile;
import com.mohaymen.model.Status;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ProfileRepository;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

import com.mohaymen.noName.Salt;

@Service
public class AccessService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    public AccessService(AccountRepository accountRepository, ProfileRepository profileRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    private Profile profileExists(String username){
        Optional<Profile> profile = profileRepository.findByHandle(username);
        return profile.orElse(null);
    }

    private Account emailExists(String email){
        Optional<Account> account = accountRepository.findByEmail(email);
        return account.orElse(null);
    }

    public Boolean infoValidation(String username, String email) {
        Profile profile = profileExists(username);
        //duplicate username
        if(profile != null)
            return false;

        //duplicate email
        Account account = emailExists(email);
        if(account == null)
            return true;
        System.out.println(account.getEmail());
        return false;
    }

    public Boolean signUp(String name, String email, byte[] password) {
        String username = email;

        if(!infoValidation(username, email))
            return false;

        Profile profile = new Profile();
        profile.setHandle(username);
        profile.setProfileName(name);
        profile.setType(ChatType.USER);
        profileRepository.save(profile);

        byte[] salt = createSalt();

        Account account = new Account();
        account.setProfile(profile);
        account.setEmail(email);
        account.setLastSeen(LocalDateTime.now());
        account.setPassword(configPassword(password, salt));
        account.setStatus(Status.DEFAULT);
        account.setSalt(salt);
        accountRepository.save(account);
        return true;
    }

    private byte[] createSalt(){
        return Salt.getSaltArray();
    }

    @SneakyThrows
    public byte[] configPassword(byte[] password, byte[] saltArray) {
        byte[] combined = new byte[password.length + saltArray.length];

        System.arraycopy(password, 0, combined, 0, password.length);
        System.arraycopy(saltArray, 0, combined, password.length, saltArray.length);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        combined = messageDigest.digest(combined);
        return combined;
    }

}

