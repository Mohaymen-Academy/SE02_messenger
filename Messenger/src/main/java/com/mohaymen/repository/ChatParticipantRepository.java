package com.mohaymen.repository;

import com.mohaymen.model.ChatParticipant;
import com.mohaymen.model.ChatParticipantID;
import com.mohaymen.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantID> {

    List<ChatParticipant> findByUser(Profile user);
    List<ChatParticipant> findByIsPinnedAndUser( boolean isPinned,Profile profile);
}
