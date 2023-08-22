package com.mohaymen.service;

import com.mohaymen.model.entity.Block;
import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.repository.BlockRepository;
import com.mohaymen.repository.ChatParticipantRepository;
import com.mohaymen.repository.MessageRepository;
import com.mohaymen.repository.ProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PinMessageService extends PinService {


    protected PinMessageService(ChatParticipantRepository cpRepository,
                                BlockRepository blockRepository,
                                ProfileRepository profileRepository,
                                MessageRepository messageRepository) {
        super(cpRepository,
                blockRepository,
                profileRepository, messageRepository);
    }

    //todo is pin message available in a closed group or channel?
    //pin a message is available for a deleted account in telegram!
    //check when block user handled
    //can someone pin a message without seeing it?is it handled in front?
    //how does pin work?
    //an admin can pin a message for every one in chat
    //in pvs both side pin for each other,no option for pinning for yourself yet
    public void setPinMessage(Long userID, Long messageId, boolean pin) throws Exception {
        Message message = checkIsPossible(userID, messageId);
        Profile user = getProfile(userID);
        Profile chat = message.getReceiver().getProfileID().equals(userID) ? message.getSender() : message.getReceiver();
        if (!pin)
            message = null;
        if (chat.getType() == ChatType.USER) {
            ChatParticipant chatParticipant1 = null;
            ChatParticipant chatParticipant2 = null;
            try {
                chatParticipant1 = getParticipant(user, chat);
            } catch (Exception ignore) {
            }
            Block block1 = getBlockParticipant(user, chat);
            Block block2 = getBlockParticipant(chat, user);
            if (block1 == null && block2 == null) {
                if (chatParticipant1 != null) {
                    chatParticipant1.setPinnedMessage(message);
                    cpRepository.save(chatParticipant1);
                }
                try {
                    chatParticipant2 = getParticipant(chat, user);
                } catch (Exception ignore) {
                }
                if (chatParticipant2 != null) {
                    chatParticipant2.setPinnedMessage(message);
                    cpRepository.save(chatParticipant2);
                }
            } else
                throw new Exception("could not pin the message,due to block");
        } else {
            List<ChatParticipant> destinations = cpRepository.findByDestination(chat);
            for (ChatParticipant p : destinations) {
                p.setPinnedMessage(message);
                cpRepository.save(p);
            }
        }
    }

    public Message getPinMessage(Long userId, Long chatId) throws Exception {
        Profile user = getProfile(userId);
        Profile chat = getProfile(chatId);
        ChatParticipant chatParticipant = getParticipant(user, chat);
        return chatParticipant.getPinnedMessage();
    }


}
