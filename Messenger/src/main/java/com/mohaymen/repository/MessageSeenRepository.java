package com.mohaymen.repository;

import com.mohaymen.model.entity.MessageSeen;
import com.mohaymen.model.supplies.ProfilePareId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageSeenRepository extends JpaRepository<MessageSeen, ProfilePareId> {
}
