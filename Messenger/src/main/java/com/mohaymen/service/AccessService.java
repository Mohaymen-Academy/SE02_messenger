package com.mohaymen.service;

import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.json_item.LoginInfo;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.supplies.Status;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.ProfilePictureRepository;
import com.mohaymen.repository.ProfileRepository;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.security.PasswordHandler;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import com.mohaymen.security.SaltGenerator;
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

    public AccessService(AccountRepository accountRepository, AccountService accountService, ProfileRepository profileRepository,
                         ProfilePictureRepository profilePictureRepository, SearchService searchService, ChatParticipantRepository cpRepository, MessageService messageService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.profileRepository = profileRepository;
        this.profilePictureRepository = profilePictureRepository;
        this.searchService = searchService;
        this.cpRepository = cpRepository;
        this.messageService = messageService;
    }

    public LoginInfo login(String email, byte[] password) throws Exception {
        Optional<Account> account = accountRepository.findByEmail(email);
        if (account.isEmpty())
            throw new Exception("Account not found");

        byte[] checkPassword = PasswordHandler.getHashed(
                PasswordHandler.combineArray(password, account.get().getSalt()));

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

    private Account emailExists(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        return account.orElse(null);
    }

    public Boolean infoValidation(String email) {
        Account account = emailExists(email);
        return account == null;
    }

    public LoginInfo signup(String name, String email, byte[] password) throws Exception {
        if (!infoValidation(email))
            throw new Exception("information is not valid");

        Profile profile = new Profile();
        profile.setHandle(email);
        profile.setProfileName(name);
        profile.setType(ChatType.USER);
        profile.setDefaultProfileColor(generateColor(email));
        profileRepository.save(profile);

        byte[] salt = SaltGenerator.getSaltArray();

        Account account = new Account();
        account.setId(profile.getProfileID());
        account.setProfile(profile);
        account.setEmail(email);
        account.setLastSeen(LocalDateTime.now());
        account.setPassword(PasswordHandler.configPassword(password, salt));
        account.setStatus(Status.DEFAULT);
        account.setSalt(salt);
        accountRepository.save(account);

        // add user to search index
        searchService.addUser(account);
        //add user to the messenger channel
        MessengerBasics(profile);
        return LoginInfo.builder()
                .message("success")
                .jwt(JwtHandler.generateAccessToken(profile.getProfileID()))
                .profile(account.getProfile())
                .lastSeen(accountService.getLastSeen(account.getId()))
                .build();
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

        byte[] checkPassword = PasswordHandler.getHashed(
                PasswordHandler.combineArray(password, account.getSalt()));

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

}