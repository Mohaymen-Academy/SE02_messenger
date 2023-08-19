package com.mohaymen.service;

import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ServerService {

    private final MessageRepository messageRepository;
    private final ProfileRepository profileRepository;
    private Profile server;


    public ServerService(ProfileRepository profileRepository,
                         MessageRepository messageRepository) {
        this.profileRepository = profileRepository;
        this.messageRepository = messageRepository;
        if (profileRepository.findById(1L).isPresent())
            server = profileRepository.findById(1L).get();
        else{
            createServerAccount();
        }
    }

    private void createServerAccount() {
        server = new Profile();
        server.setProfileID(1L);
        server.setHandle("#Server");
        server.setType(ChatType.SERVER);
        server.setProfileName("SERVER");
        server.setDeleted(false);
        profileRepository.save(server);
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
