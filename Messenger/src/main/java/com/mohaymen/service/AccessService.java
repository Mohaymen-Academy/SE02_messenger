package com.mohaymen.service;

import com.mohaymen.model.entity.*;
import com.mohaymen.repository.*;
import com.mohaymen.model.supplies.*;
import java.awt.*;
import java.util.*;
import com.mohaymen.model.json_item.LoginInfo;
import com.mohaymen.security.JwtHandler;
import com.mohaymen.security.PasswordHandler;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.mohaymen.security.SaltGenerator;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessService {
    private final AccountRepository accountRepository;
    private final ChatParticipantService chatParticipantService;

    private final AccountService accountService;

    private final ProfileRepository profileRepository;
    private final ServerService serverService;

    private final ProfilePictureRepository profilePictureRepository;

    private final SearchService searchService;

    private final ChatParticipantRepository cpRepository;

    private final MessageService messageService;

    public AccessService(AccountRepository accountRepository, ChatParticipantService chatParticipantService, AccountService accountService, ProfileRepository profileRepository,
                         ServerService serverService, ProfilePictureRepository profilePictureRepository, SearchService searchService, ChatParticipantRepository cpRepository, MessageService messageService) {
        this.accountRepository = accountRepository;
        this.chatParticipantService = chatParticipantService;
        this.accountService = accountService;
        this.profileRepository = profileRepository;
        this.serverService = serverService;
        this.profilePictureRepository = profilePictureRepository;
        this.searchService = searchService;
        this.cpRepository = cpRepository;
        this.messageService = messageService;
    }


    public boolean emailExists(String email) {
        Optional<Account> account = accountRepository.findByEmail(email);
        return account.isPresent();
    }

    public LoginInfo signup(String name, String email, byte[] password) throws Exception {
        if (emailExists(email))
            throw new Exception("information is not valid");

        Profile profile = new Profile(email, name, ChatType.USER, generateColor(email));
        profileRepository.save(profile);

        byte[] salt = SaltGenerator.getSaltArray();

        Account account = new Account(profile.getProfileID(), profile,
                PasswordHandler.configPassword(password, salt), email,
                Status.DEFAULT, LocalDateTime.now(), false, salt);
        accountRepository.save(account);

        // add user to search index
        searchService.addUser(account);
        //add user to the messenger channel

        welcomeUserInitialize(profile);
        return LoginInfo.builder()
                .message("success")
                .jwt(JwtHandler.generateAccessToken(profile.getProfileID()))
                .profile(account.getProfile())
                .lastSeen(accountService.getLastSeen(account.getId()))
                .build();
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

    private void welcomeUserInitialize(Profile profile) throws Exception {
        Profile baseChannel = profileRepository.findById(3L).get();
        Profile baseAccount = profileRepository.findById(2L).get();
        try {
            messageService.sendMessage(baseAccount.getProfileID(), profile.getProfileID(), "به پیام رسان رسا خوش آمدید", "", null, null, null);
            chatParticipantService.createChatParticipant(profile,baseChannel,false);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    public static String generateColor(String inputString) {
        int seed = inputString.hashCode();
        Random random = new Random(seed);
        int hue = random.nextInt(360);
        Color color = Color.getHSBColor(hue / 360f, 0.5f, 0.9f);
        return String.format("#%06x", color.getRGB() & 0x00FFFFFF);
    }

}