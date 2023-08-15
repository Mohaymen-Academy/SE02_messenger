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
        if (profileRepository.findById(1L).isPresent())
            server = profileRepository.findById(1L).get();
    }

    public void sendMessage(String messageText, Profile receiver) {
        if (server == null) return;
        Message message = new Message();
        message.setSender(server);
        message.setReceiver(receiver);
        message.setText(messageText);
        message.setTime(LocalDateTime.now());
        message.setViewCount(0);
        messageRepository.save(message);
    }
}
