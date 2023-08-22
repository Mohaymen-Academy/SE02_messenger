package com.mohaymen.service;

import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.Status;
import com.mohaymen.security.PasswordHandler;
import com.mohaymen.security.SaltGenerator;
import com.mohaymen.repository.AccountRepository;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Service
public class ServerService {
    private final ProfileRepository profileRepository;
    private final MessageRepository messageRepository;
    private final ChatParticipantRepository cpRepository;
    private final AccountRepository accountRepository;
    private final SearchService searchService;
    private Profile server;
    private Profile baseChannel;
    private Profile baseProfile;


    public ServerService(ProfileRepository profileRepository,
                         MessageRepository messageRepository,
                         ChatParticipantRepository cpRepository,
                         AccountRepository accountRepository,
                         SearchService searchService) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        this.accountRepository = accountRepository;
        this.searchService = searchService;
        this.profileRepository = profileRepository;
        Optional<Profile> server_tmp = profileRepository.findById(1L);
        Optional<Profile> baseAccount_tmp = profileRepository.findById(2L);
        Optional<Profile> baseChannel_tmp = profileRepository.findById(3L);
        if (server_tmp.isEmpty())
            server = createServerAccounts(profileRepository, false, 3, 1L, "SERVER", "#SERVER");
        else
            server = server_tmp.get();
        if (baseAccount_tmp.isEmpty()) {
            baseProfile = createServerAccounts(profileRepository, false, 0, 2L, "اعلان های رسا", "#MESSENGER-BASE-ACCOUNT");
            baseProfile.setDefaultProfileColor("#295c4c");
            baseProfile.setBiography("کانال رسمی پیام رسان رسا");
            profileRepository.save(baseProfile);
            Account account = new Account();
            account.setId(baseProfile.getProfileID());
            account.setProfile(baseProfile);
            account.setEmail("rasaa.messenger.team@gamil.com");
            account.setLastSeen(LocalDateTime.now());
            account.setStatus(Status.DEFAULT);
            byte[] accountSalt = SaltGenerator.getSaltArray();
            byte[] password = "kimia ali sara sana".getBytes();
            account.setSalt(accountSalt);
            account.setPassword(PasswordHandler.configPassword(password, accountSalt));
            accountRepository.save(account);
            searchService.addUser(account);

        } else
            baseProfile = baseAccount_tmp.get();
        if (baseChannel_tmp.isEmpty()) {
            baseChannel = createServerAccounts(profileRepository, false, 2, 3L, "پیام رسان رسا", "#MESSENGER-BASE-CHANNEL");
            sendMessage("این کانال ساخته شد", baseChannel);
            cpRepository.save(new ChatParticipant(baseProfile, baseChannel, baseChannel.getHandle(), true));
            baseChannel.setDefaultProfileColor("#59b35f");
            baseChannel.setMemberCount(1);
            profileRepository.save(baseChannel);
        } else
            baseChannel = baseChannel_tmp.get();
        searchService.addChannel(baseChannel);

    }

    private Profile createServerAccounts(ProfileRepository profileRepository, boolean isDeleted, int type,
                                         Long profileId, String profileName, String handle) {
        return profileRepository.insertProfile(isDeleted, type, profileId, profileName, handle);
    }

    public void sendMessage(String messageText, Profile receiver) {
        Message message = new Message();
        message.setSender(server);
        message.setReceiver(receiver);
        message.setText(messageText);
        message.setTime(Instant.now());
        message.setViewCount(0);
        messageRepository.save(message);
    }
}
