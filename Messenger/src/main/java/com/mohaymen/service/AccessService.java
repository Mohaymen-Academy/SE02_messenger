package com.mohaymen.service;

import com.mohaymen.model.Account;
import com.mohaymen.model.ChatType;
import com.mohaymen.model.Profile;
import com.mohaymen.model.Status;
import com.mohaymen.model.AccessToken;
import com.mohaymen.model.Account;
import com.mohaymen.repository.AccessTokenRepository;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ProfileRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import com.mohaymen.noName.Salt;

@Service
public class AccessService {

    private static final String SECRET_KEY = "2BONtKNaqHAMhbigbtitKmQRDf3iIysttFlJ8BQ2ed5uaErlzUMS0Kcq66p5rDko+BRT2pfCcTSS3CdeZKZaVapj3p2LztPU7yrlJrVZOMo=";

    private final AccountRepository accountRepository;
    private final ProfileRepository profileRepository;
    private final AccessTokenRepository accessTokenRepository;

    public AccessService(AccountRepository accountRepository, ProfileRepository profileRepository, AccessTokenRepository accessTokenRepository) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.accessTokenRepository = accessTokenRepository;
    }

    @Transactional

    public String login(String email, byte[] password, String ip) throws Exception {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            throw new Exception("User not found");

        byte[] checkPassword = getHashed(combineArray(password, account.get().getSalt()));

        if (!Arrays.equals(checkPassword, account.get().getPassword()))
            throw new Exception("Wrong password");

        Optional<AccessToken> accessToken = accessTokenRepository.findByIp(ip);

        accessToken.ifPresent(token -> accessTokenRepository.deleteByIp(token.getIp()));


        Date expirationTime = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));

        String jwtToken = Jwts.builder()
                .claim("id", account.get().getId())
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();


        AccessToken newAT = new AccessToken(jwtToken, account.get().getProfile(), expirationTime, ip);
        accessTokenRepository.save(newAT);

        return jwtToken;
    }

    private Profile profileExists(String username) {
        Optional<Profile> profile = profileRepository.findByHandle(username);
        if (profile.isEmpty())
            return null;
        return profile.get();
    }

    private Account emailExists(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            return null;
        return account.get();
    }

    public Boolean infoValidation(String username, String email) {
        Profile profile = profileExists(username);
        //duplicate username
        if (profile != null)
            return false;

        //duplicate email
        Account account = emailExists(email);
        if (account == null)
            return true;
        System.out.println(account.getEmail());
        return false;
    }

    public Boolean signUp(String name, String email, byte[] password) {
        String username = email;

        if (!infoValidation(username, email))
            return false;

        Profile profile = new Profile();
        profile.setHandle(username);
        profile.setProfileName(name);
        profile.setType(ChatType.USER);
        profileRepository.save(profile);

        byte[] salt = Salt.getSaltArray();

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


