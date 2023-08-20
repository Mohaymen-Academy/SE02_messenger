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

    public ServerService(ProfileRepository profileRepository,
                         MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
//        createServerAccounts(profileRepository,);
    }

    private void createServerAccounts(ProfileRepository profileRepository, boolean isDeleted, int type,
                                      Long profileId, String profileName, String handle){
        if (profileRepository.findById(profileId).isPresent())
            server = profileRepository.findById(profileId).get();
        else server = profileRepository.createServer(isDeleted, type, profileId, profileName, handle);
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
