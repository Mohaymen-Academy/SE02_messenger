package com.mohaymen.service;

import com.mohaymen.model.Account;
import com.mohaymen.model.ChatType;
import com.mohaymen.model.Profile;
import com.mohaymen.model.Status;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ProfileRepository;
import com.mohaymen.security.JwtHandler;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import com.mohaymen.security.SaltGenerator;

@Service
public class AccessService {

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;

    public AccessService(AccountRepository accountRepository, ProfileRepository profileRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
    }

    public String login(String email, byte[] password, String ip) throws Exception {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            throw new Exception("User not found");

        byte[] checkPassword = getHashed(combineArray(password, account.get().getSalt()));

        if (!Arrays.equals(checkPassword, account.get().getPassword()))
            throw new Exception("Wrong password");

        return JwtHandler.generateAccessToken(account.get().getId());
    }

    private Profile profileExists(String username) {
        Optional<Profile> profile = profileRepository.findByHandle(username);
        return profile.orElse(null);
    }

    private Account emailExists(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        return account.orElse(null);
    }

    public Boolean infoValidation(String email) {
        //duplicate email
        Account account = emailExists(email);
        if(account == null)
            return true;
        System.out.println(account.getEmail());
        return false;
    }

    public Boolean signUp(String name, String email, byte[] password) {
        if(!infoValidation(email))
            return false;

        Profile profile = new Profile();
        profile.setHandle(email);
        profile.setProfileName(name);
        profile.setType(ChatType.USER);
        profile.setDefaultProfileColor(generateColor(email));
        profileRepository.save(profile);

        byte[] salt = SaltGenerator.getSaltArray();

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

    private Color generateColor(String inputString) {
        int seed = inputString.hashCode();
        Random random = new Random(seed);
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return new Color(red, green, blue);
    }

    public byte[] configPassword(byte[] password, byte[] saltArray) {
        byte[] combined = combineArray(password, saltArray);
        return getHashed(combined);
    }

    public byte[] combineArray(byte[] arr1, byte[] arr2) {
        byte[] combined = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, combined, 0, arr1.length);
        System.arraycopy(arr2, 0, combined, arr1.length, arr2.length);
        return combined;
    }

    @SneakyThrows
    public byte[] getHashed(byte[] bytes) {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        return messageDigest.digest(bytes);
    }

}