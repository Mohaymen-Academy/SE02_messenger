package com.mohaymen.repository;

import com.mohaymen.model.ProfilePicture;
import com.mohaymen.model.ProfilePictureID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, ProfilePictureID> {
}
