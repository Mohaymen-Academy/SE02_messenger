package com.mohaymen.repository;

import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByHandle(String handle);

    Optional<Profile> findByTypeAndHandle(ChatType type, String handle);

    boolean existsByHandleAndProfileIDNot(String handle, Long id);

    @Query(value = "insert into profile (is_deleted, type, profile_id, profile_name, handle) \n" +
            "values ('false', 3, 1, 'SERVER', '#SERVER') RETURNING *", nativeQuery = true)
    @Transactional
    Profile createServer();

}
