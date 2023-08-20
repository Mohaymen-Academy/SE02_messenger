package com.mohaymen.repository;

import com.mohaymen.model.supplies.ChatType;
import com.mohaymen.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByHandle(String handle);

    Optional<Profile> findByTypeAndHandle(ChatType type, String handle);

    boolean existsByHandleAndProfileIDNot(String handle, Long id);

    @Query("insert into profile (is_deleted, type, profile_id, profile_name, handle) \n" +
            "values (:is_deleted, :type, :profile_id, :profile_name, :handle) RETURNING *")
    @Transactional
    Profile createServer(@Param(value = "is_deleted") boolean isDeleted,
                         @Param(value = "type") int type,
                         @Param(value = "profile_id") Long profileId,
                         @Param(value = "profile_name") String profileName,
                         @Param(value = "handle") String handle);

}
