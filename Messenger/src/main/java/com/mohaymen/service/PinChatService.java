package com.mohaymen.service;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.repository.BlockRepository;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class PinChatService extends PinService {

    public PinChatService(ChatParticipantRepository cpRepository,
                          BlockRepository blockRepository,
                          ProfileRepository profileRepository,
                          MessageRepository messageRepository) {
        super(cpRepository, blockRepository, profileRepository, messageRepository);
    }

    public void pinChat(long userId, long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant chatParticipant = getParticipant(user, chat);
        chatParticipant.setPinned(true);
        cpRepository.save(chatParticipant);
    }

    public void unpinChat(long userId, long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant chatParticipant = getParticipant(user, chat);
        chatParticipant.setPinned(false);
        cpRepository.save(chatParticipant);
    }

}
