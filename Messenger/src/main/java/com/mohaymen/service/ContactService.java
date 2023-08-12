package com.mohaymen.service;

import com.mohaymen.model.ChatType;
import com.mohaymen.model.ContactID;
import com.mohaymen.model.ContactList;
import com.mohaymen.model.Profile;
import com.mohaymen.repository.ContactRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final ProfileRepository profileRepository;

    public ContactService(ContactRepository contactRepository, ProfileRepository profileRepository){
        this.contactRepository = contactRepository;
        this.profileRepository = profileRepository;
    }

    private ContactList contactExists(ContactID contactID){
        Optional<ContactList> contactList = contactRepository.findById(contactID);
        return contactList.orElse(null);
    }

    private Profile getValidContact(String currentUsername, String username){
        if(currentUsername.equals(username))
            return null;
        Optional<Profile> optionalProfile = profileRepository.findByHandle(username);
        if(optionalProfile.isEmpty())
            return null;
        Profile contact = optionalProfile.get();
        if(contact.getType() != ChatType.USER)
            return null;
        return contact;
    }

    public Profile addContact(Long firstUserID, String secondUsername, String customName){
        ContactList contactList = new ContactList();
        Profile firstProfile = profileRepository.findById(firstUserID).get();
        Profile secondProfile = getValidContact(firstProfile.getHandle(), secondUsername);
        ContactID contactID = new ContactID(firstProfile, secondProfile);
        if(contactExists(contactID) != null)
            return null;
        contactList.setFirstUser(firstProfile);
        contactList.setSecondUser(secondProfile);
        contactList.setCustomName(customName);
        contactRepository.save(contactList);

        return getProfileWithCustomName(firstProfile, secondProfile);
    }

    public List<Profile> getContactsOfOneUser(Long id){
        List<ContactList> contacts = contactRepository.findByFirstUser_ProfileID(id);
        List<Profile> profileDisplays = new ArrayList<>();
        for (ContactList contactList : contacts){
            Profile contact = getProfileWithCustomName(contactList.getFirstUser(), contactList.getSecondUser());
            profileDisplays.add(contact);
        }
        return profileDisplays;
    }

    public Profile getProfileWithCustomName(Profile firstUser, Profile secondUser){
        ContactID contactID = new ContactID(firstUser, secondUser);
        Optional<ContactList> contactListOptional = contactRepository.findById(contactID);
        if(contactListOptional.isEmpty())
            return secondUser;
        secondUser.setProfileName(contactListOptional.get().getCustomName());
        return secondUser;
    }
}
