package com.mohaymen.service;

import com.mohaymen.repository.ProfilePictureRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfilePictureService {

    private ProfilePictureRepository profilePictureRepository;

    public ProfilePictureService(ProfilePictureRepository profilePictureRepository){
        this.profilePictureRepository = profilePictureRepository;
    }

//    public boolean addProfilePicture(){
//
//
//    }
}
