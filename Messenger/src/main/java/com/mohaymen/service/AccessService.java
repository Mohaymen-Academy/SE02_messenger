package com.mohaymen.service;

import com.mohaymen.model.Account;
import com.mohaymen.model.ChatType;
import com.mohaymen.model.Profile;
import com.mohaymen.model.Status;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ProfileRepository;
import com.mohaymen.security.JwtHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import com.mohaymen.security.SaltGenerator;

@Service
public class AccessService {

    private final AccountRepository accountRepository;

    private final ProfileRepository profileRepository;

    private JavaMailSender mailSender;

    public AccessService(AccountRepository accountRepository, ProfileRepository profileRepository, JavaMailSender mailSender) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.mailSender = mailSender;
    }

    public String login(String email, byte[] password) throws Exception {
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
        Account account = emailExists(email);
        if(account == null)
            return true;
        return false;
    }

    public boolean signup(String name, String email, byte[] password, String inputCode) throws MessagingException, UnsupportedEncodingException {
        if(!infoValidation(email))
            return false;

        Profile profile = new Profile();
        profile.setHandle(email);
        profile.setProfileName(name);
        profile.setType(ChatType.USER);
        profile.setDefaultProfileColor(generateColor(email));

        byte[] salt = SaltGenerator.getSaltArray();

        Account account = new Account();
        account.setProfile(profile);
        account.setEmail(email);
        account.setLastSeen(LocalDateTime.now());
        account.setPassword(configPassword(password, salt));
        account.setStatus(Status.DEFAULT);
        account.setSalt(salt);

        int verificationCode = convertEmailToFourDigitNumber(email);
        sendVerificationEmail(account, String.valueOf(verificationCode));

        return verify(account, profile, inputCode);
    }

    private int convertEmailToFourDigitNumber(String email) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(email.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);
            String hashedEmail = no.toString(16);

            String subHashedEmail = hashedEmail.substring(0, 4);
            int fourDigitNumber = Math.abs(Integer.parseInt(subHashedEmail, 16));

            return fourDigitNumber % 10000;
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return -1; // Return a default value in case of an error
    }

    private void sendVerificationEmail(Account account, String code) throws MessagingException, UnsupportedEncodingException {
        String toAddress = account.getEmail();
        String fromAddress = "rasaa.messenger@gmail.com";
        String senderName = "Rasaa Messenger";
        String subject = "Please verify your registration";
        String content = "Dear [[name]],<br>"
                + "Your verification code::<br>"
                + "<h3>[[code]]</h3>"
                + "Thank you,<br>"
                + "Rasaa Messenger.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", account.getProfile().getProfileName());
        content = content.replace("[[code]]", code);

        helper.setText(content, true);

        mailSender.send(message);
    }

    private boolean verify(Account account, Profile profile, String inputCode) {
        String actualCode = String.valueOf(convertEmailToFourDigitNumber(account.getEmail()));
        if(inputCode.equals(actualCode)) {
            accountRepository.save(account);
            profileRepository.save(profile);
            return true;
        }
        return false;
    }

    public Profile deleteProfile(Profile profile){
        UUID uuid = UUID.randomUUID();
        profile.setHandle(profile.getHandle() + uuid);
        profile.setDeleted(true);
        profileRepository.save(profile);
        return profile;
    }

    public void deleteAccount(Long id, byte[] password) throws Exception {
        Profile profile = deleteProfile(profileRepository.findById(id).get());
        Account account = accountRepository.findByProfile(profile).get();

        byte[] checkPassword = getHashed(combineArray(password, account.getSalt()));

        if (!Arrays.equals(checkPassword, account.getPassword()))
            throw new Exception("Wrong password");

        accountRepository.delete(account);
    }

    public static String generateColor(String inputString) {
        int seed = inputString.hashCode();
        Random random = new Random(seed);
        int hue = random.nextInt(360);
        Color color = Color.getHSBColor(hue / 360f,0.5f, 0.9f);
        return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
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