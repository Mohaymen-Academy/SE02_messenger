package com.mohaymen.repository;

import com.mohaymen.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByHandle(String handle);

    boolean existsByHandleAndProfileIDNot(String handle, Long id);
}
