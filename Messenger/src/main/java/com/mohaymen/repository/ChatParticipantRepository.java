package com.mohaymen.repository;

import com.mohaymen.model.ChatParticipant;
import com.mohaymen.model.ChatParticipantID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantID> {
}
