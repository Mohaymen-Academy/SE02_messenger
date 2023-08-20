package com.mohaymen.service;

import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ServerService {

    private final MessageRepository messageRepository;
    private Profile server;
    private Profile baseChannel;
    private Profile baseAccount;

    public ServerService(ProfileRepository profileRepository,
                         MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        if (profileRepository.findById(1L).isEmpty())
            server = createServerAccounts(profileRepository, false, 3, 1L, "SERVER", "#SERVER");
        if (profileRepository.findById(2L).isEmpty())
            baseAccount = createServerAccounts(profileRepository, false, 0, 2L, "اعلان های رسا", "#MESSENGER-BASE-ACCOUNT");
        if (profileRepository.findById(3L).isEmpty())
            baseChannel = createServerAccounts(profileRepository, false, 2, 3L, "پیام رسان رسا", "#MESSENGER-BASE-CHANNEL");
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
