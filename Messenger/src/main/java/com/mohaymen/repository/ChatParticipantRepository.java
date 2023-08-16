package com.mohaymen.repository;

import com.mohaymen.model.entity.ChatParticipant;
import com.mohaymen.model.supplies.ProfilePareId;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ProfilePareId> {

    List<ChatParticipant> findByUser(Profile user);

    List<ChatParticipant> findByDestination(Profile destination);

    ChatParticipant findByDestinationAndUser(Profile destination, Profile user);
}
