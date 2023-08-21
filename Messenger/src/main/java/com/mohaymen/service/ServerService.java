package com.mohaymen.service;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.Status;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Service
public class ServerService {

    private final MessageRepository messageRepository;
    private final ChatParticipantRepository cpRepository;
    private Profile server;
    private Profile baseChannel;
    private Profile baseAccount;

    public ServerService(ProfileRepository profileRepository,
                         MessageRepository messageRepository, ChatParticipantRepository cpRepository) {
        this.messageRepository = messageRepository;
        this.cpRepository = cpRepository;
        Optional<Profile> server_tmp = profileRepository.findById(1L);
        Optional<Profile> baseAccount_tmp = profileRepository.findById(2L);
        Optional<Profile> baseChannel_tmp = profileRepository.findById(3L);
        if (server_tmp.isEmpty())
            server = createServerAccounts(profileRepository, false, 3, 1L, "SERVER", "#SERVER");
        else
            server = server_tmp.get();
        if (baseAccount_tmp.isEmpty()) {
            baseAccount = createServerAccounts(profileRepository, false, 0, 2L, "اعلان های رسا", "#MESSENGER-BASE-ACCOUNT");
            baseAccount.setDefaultProfileColor("#808000");
            profileRepository.save(baseAccount);
        } else
            baseAccount = baseAccount_tmp.get();
        if (baseChannel_tmp.isEmpty()) {
            baseChannel = createServerAccounts(profileRepository, false, 2, 3L, "پیام رسان رسا", "#MESSENGER-BASE-CHANNEL");
            sendMessage("این کانال ساخته شد", baseChannel);
            cpRepository.save(new ChatParticipant(baseAccount, baseChannel, baseChannel.getHandle(), true));
            baseChannel.setDefaultProfileColor("#008000");
            baseChannel.setMemberCount(1);
            profileRepository.save(baseChannel);
        } else
            baseChannel = baseChannel_tmp.get();

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
        message.setTime(LocalDateTime.now());
        message.setViewCount(0);
        messageRepository.save(message);
    }
}
