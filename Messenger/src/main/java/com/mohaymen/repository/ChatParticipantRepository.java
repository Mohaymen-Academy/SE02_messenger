package com.mohaymen.repository;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ProfilePareId> {

    List<ChatParticipant> findByUser(Profile user);
    void deleteByDestination(Profile destination);

    List<ChatParticipant> findByDestination(Profile destination);
    void deleteByPinnedMessageAndDestination(Message msg,Profile destination);

    List<ChatParticipant> findByChatId(String chatId);

    ChatParticipant findByDestinationAndUser(Profile destination, Profile user);

}
