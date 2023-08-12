package com.mohaymen.repository;

import com.mohaymen.model.MessageSeen;
import com.mohaymen.model.ProfilePareId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageSeenRepository extends JpaRepository<MessageSeen, ProfilePareId> {
}
