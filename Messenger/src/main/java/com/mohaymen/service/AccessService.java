package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.model.json_item.LoginInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.Status;
import com.mohaymen.model.supplies.security.SaltGenerator;
import com.mohaymen.repository.*;
import com.mohaymen.model.supplies.security.JwtHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessService {

    private final AccountRepository accountRepository;

    private final AccountService accountService;

    private final ProfileRepository profileRepository;

    private final ProfilePictureRepository profilePictureRepository;

    private final SearchService searchService;

    private final ChatParticipantRepository cpRepository;

    private final MessageService messageService;

    private JavaMailSender mailSender;

    public AccessService( AccountRepository accountRepository, AccountService accountService, ProfileRepository profileRepository,
                         ProfilePictureRepository profilePictureRepository, SearchService searchService,
                          ChatParticipantRepository cpRepository, MessageService messageService,
                          JavaMailSender mailSender) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.profileRepository = profileRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.searchService = searchService;
        this.cpRepository = cpRepository;
        this.messageService = messageService;
        this.mailSender = mailSender;
    }

    public LoginInfo login(String email, byte[] password) throws Exception {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            throw new Exception("User not found");
        System.out.println(password);
        byte[] checkPassword = getHashed(combineArray(password, account.get().getSalt()));

        if (!Arrays.equals(checkPassword, account.get().getPassword()))
            throw new Exception("Wrong password");
        accountService.UpdateLastSeen(account.get().getId());
        return LoginInfo.builder()
                .message("success")
                .jwt(JwtHandler.generateAccessToken(account.get().getId()))
                .profile(account.get().getProfile())
                .lastSeen(accountService.getLastSeen(account.get().getId()))
                .build();
    }

    public LoginInfo verify(String email, String name, byte[] password, String inputCode) throws Exception {
        String actualCode = convertEmailToFourDigitNumber(email);
        if(inputCode.equals(actualCode)) {
            Profile profile = createProfile(email, name);
            Account account = createAccount(profile, email, password);
            return completeSignup(account, profile);
        }
        throw new Exception("Information is not valid.");
    }

    private LoginInfo completeSignup(Account account, Profile profile){
        accountRepository.save(account);
        profileRepository.save(profile);
        //add user to search index
        searchService.addUser(account);
        //add user to the messenger channel
        MessengerBasics(profile);
        return LoginInfo.builder()
                .message("success")
                .jwt(JwtHandler.generateAccessToken(account.getId()))
                .profile(account.getProfile())
                .lastSeen(accountService.getLastSeen(account.getId()))
                .build();
    }

    private String convertEmailToFourDigitNumber(String email) {
        email = email + LocalDate.now();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(email.getBytes());

            BigInteger no = new BigInteger(1, messageDigest);
            String hashedEmail = no.toString(16);
            return hashedEmail.substring(0, 4);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return ""; // Return a default value in case of an error
    }

    private void sendEmail(String name, String email, String code, String emailText, String emailSubject) throws MessagingException, UnsupportedEncodingException {
        String fromAddress = "rasaa.messenger.team@gmail.com";
        String senderName = "پیامرسان رسا";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true, "UTF-8");

        helper.setFrom(fromAddress, senderName);
        helper.setTo(email);
        helper.setSubject(emailSubject);

        System.out.println(name);

        if(emailText.contains("[[name]]"))
            emailText = emailText.replace("[[name]]", name);
        if(emailText.contains("[[code]]"))
            emailText = emailText.replace("[[code]]", code);

        helper.setText(emailText, true);

        mailSender.send(message);
    }

    private Account emailExists(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        return account.orElse(null);
    }

    public Account infoValidation(String email) {
        return emailExists(email);
    }

    private Profile createProfile(String handle, String name){
        Profile profile = new Profile();
        profile.setHandle(handle);
        profile.setProfileName(name);
        profile.setType(ChatType.USER);
        profile.setDefaultProfileColor(generateColor(handle));
        return profile;
    }

    private Account createAccount(Profile profile, String email, byte[] password){
        byte[] salt = SaltGenerator.getSaltArray();
        Account account = new Account();
        account.setProfile(profile);
        account.setEmail(email);
        account.setLastSeen(LocalDateTime.now());
        account.setPassword(configPassword(password, salt));
        account.setStatus(Status.DEFAULT);
        account.setSalt(salt);
        return account;
    }

    public void signup(String name, String email) throws Exception {
        if (infoValidation(email) != null)
            throw new Exception("information is not valid");
        String verificationCode = convertEmailToFourDigitNumber(email);
        sendEmail(name, email, verificationCode,"<h3>[[name]] عزیز ،<br> </h3>"
                + " </h3><h3>کد تایید شما:<br>"
                + "<h1>[[code]]</h1>"
                + "<h3>باتشکر،<br>"
                + "پیامرسان رسا"
                + "</h3>", "لطفا ثبت نام خود را تایید کنید");
    }

    public void forgetPassword(String email) throws Exception {
        Account account = infoValidation(email);
        if(account == null)
            throw new Exception("you have not signed up or invalid email");
        String generatedPass = generateRandomPassword();
        byte[] salt = SaltGenerator.getSaltArray();
        account.setPassword(configPassword(generatedPass.getBytes(), salt));
        account.setSalt(salt);
        accountRepository.save(account);
        sendEmail(null, email, generatedPass, " </h3><h3>رمز عبور جدید شما:<br>"
                + "<h1>[[code]]</h1>"
                + "<h3>باتشکر،<br>"
                + "پیامرسان رسا"
                + "</h3>", "بازگردانی رمز عبور");
    }

    private String generateRandomPassword(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    private void MessengerBasics(Profile profile) {
        Profile baseChannel = profileRepository.findById(3L).get();
        Profile baseAccount = profileRepository.findById(2L).get();
        try {
            messageService.sendMessage(baseAccount.getProfileID(), profile.getProfileID(), "به پیام رسان رسا خوش آمدید", "", null, null, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
        try {
            cpRepository.save(new ChatParticipant(profile, baseChannel, baseChannel.getHandle(), false));
            baseChannel.setMemberCount(baseChannel.getMemberCount() + 1);
            profileRepository.save(baseChannel);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public void deleteProfile(Profile profile) {
        UUID uuid = UUID.randomUUID();
        profilePictureRepository.deleteByProfile(profile);
        profile.setHandle(profile.getHandle() + uuid);
        profile.setProfileName("DELETED");
        profile.setDeleted(true);
        profile.setLastProfilePicture(null);
        profileRepository.save(profile);
    }

    @Transactional
    public void deleteAccount(Long id, byte[] password) throws Exception {
        Profile profile = profileRepository.findById(id).get();
        Account account = accountRepository.findByProfile(profile).get();

        byte[] checkPassword = getHashed(combineArray(password, account.getSalt()));

        if (!Arrays.equals(checkPassword, account.getPassword()))
            throw new Exception("Wrong password");

        deleteProfile(profile);
        searchService.deleteUser(profile);
        accountRepository.delete(account);
    }

    public static String generateColor(String inputString) {
        int seed = inputString.hashCode();
        Random random = new Random(seed);
        int hue = random.nextInt(360);
        Color color = Color.getHSBColor(hue / 360f, 0.5f, 0.9f);
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