package com.mohaymen.repository;

import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.entity.ProfilePicture;
import com.mohaymen.model.supplies.ProfilePictureID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, ProfilePictureID> {

    List<ProfilePicture> findByProfile_ProfileID(Long profileId);

//    @Query("delete from ProfilePicture pp where pp.profile.profileID =: profileId")
//    void deletePP(@Param("profileId") Long title);
    @Transactional
    void deleteByProfile(Profile profile);
}
