package com.mohaymen.repository;

import com.mohaymen.model.MediaFile;
import com.mohaymen.model.Profile;
import com.mohaymen.model.ProfilePicture;
import com.mohaymen.model.ProfilePictureID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, ProfilePictureID> {

    List<ProfilePicture> findByProfile_ProfileID(Long profileId);

}
