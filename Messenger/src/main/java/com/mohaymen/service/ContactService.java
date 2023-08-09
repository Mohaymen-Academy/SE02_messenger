package com.mohaymen.service;

import com.mohaymen.model.ContactID;
import com.mohaymen.model.ContactList;
import com.mohaymen.model.Profile;
import com.mohaymen.model.ProfileDisplay;
import com.mohaymen.repository.ContactRepository;
import com.mohaymen.repository.ProfileRepository;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    private ContactRepository contactRepository;
    private ProfileRepository profileRepository;

    public ContactService(ContactRepository contactRepository, ProfileRepository profileRepository){
        this.contactRepository = contactRepository;
        this.profileRepository = profileRepository;
    }

    private ContactList contactExists(ContactID contactID){
        Optional<ContactList> contactList = contactRepository.findById(contactID);
        return contactList.orElse(null);
    }

    public ProfileDisplay addContact(Long firstUserID, String secondUsername, String customName){
        ContactList contactList = new ContactList();
        Profile firstProfile = profileRepository.findById(firstUserID).get();
        Profile secondProfile = profileRepository.findByHandle(secondUsername).get();
        ContactID contactID = new ContactID(firstProfile, secondProfile);
        if(contactExists(contactID) != null)
            return null;
        contactList.setFirstUser(firstProfile);
        contactList.setSecondUser(secondProfile);
        contactList.setCustomName(customName);
        contactRepository.save(contactList);

        return ProfileDisplay.builder()
                        .profileId(secondProfile.getProfileID())
                .color(secondProfile.getDefaultProfileColor())
                .name(getProfileDisplayName(firstProfile, secondProfile))
                .unreadMessageCount(0)
                .build();
    }

    public List<ProfileDisplay> getContactsOfOneUser(Long id
        List<ContactList> contacts = contactRepository.findByFirstUser_ProfileID(id);
        List<ProfileDisplay> profileDisplays = new ArrayList<>();
        for (ContactList contactList : contacts){
            Profile contact = contactList.getSecondUser();
            ProfileDisplay profileDisplay = new ProfileDisplay(contact.getProfileID(), contactList.getCustomName(),
                    contact.getDefaultProfileColor(), 0, null);
            profileDisplays.add(profileDisplay);
        }
        return profileDisplays;
    }
    public String getProfileDisplayName(Profile firstUser, Profile secondUser){
        ContactID contactID = new ContactID(firstUser, secondUser);
        Optional<ContactList> contactListOptional = contactRepository.findById(contactID);
        if(contactListOptional.isEmpty())
            return secondUser.getProfileName();
        return contactListOptional.get().getCustomName();
    }
}
