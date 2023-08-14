package com.mohaymen.repository;

import com.mohaymen.model.entity.ProfilePicture;
import com.mohaymen.model.supplies.ProfilePictureID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, ProfilePictureID> {

    List<ProfilePicture> findByProfile_ProfileID(Long profileId);

}
