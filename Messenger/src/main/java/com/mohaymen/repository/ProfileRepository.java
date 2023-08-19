package com.mohaymen.repository;

import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByHandle(String handle);

    Optional<Profile> findByTypeAndHandle(ChatType type, String handle);

    boolean existsByHandleAndProfileIDNot(String handle, Long id);

}
