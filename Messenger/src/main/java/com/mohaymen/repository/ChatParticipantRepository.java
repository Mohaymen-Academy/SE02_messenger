package com.mohaymen.repository;

import com.mohaymen.model.ChatParticipant;
import com.mohaymen.model.ProfilePareId;
import com.mohaymen.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ProfilePareId> {

    List<ChatParticipant> findByUser(Profile user);
}
