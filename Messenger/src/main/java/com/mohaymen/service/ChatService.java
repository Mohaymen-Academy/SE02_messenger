package com.mohaymen.service;

import com.mohaymen.model.Message;
import com.mohaymen.model.Profile;
import com.mohaymen.repository.MessageRepository;
import org.springframework.stereotype.Service;
import java.util.Iterator;

@Service
public class ChatService {

    private final MessageRepository messageRepository;

    public ChatService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public boolean sendMessage(Long sender, Long receiver, String text) {
        Message message = new Message();
        message.setSender(new Profile(){{setProfileID(sender);}});
        message.setReceiver(new Profile(){{setProfileID(receiver);}});
        message.setText(text);
        messageRepository.save(message);
        return true;
    }

//    public Iterator<Message> getMessages(Long chatID, Long userID) {
//
//    }

}
