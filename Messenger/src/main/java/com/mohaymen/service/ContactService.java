package com.mohaymen.service;

import com.mohaymen.model.ContactID;
import com.mohaymen.model.ContactList;
import com.mohaymen.model.Profile;
import com.mohaymen.repository.ContactRepository;
import com.mohaymen.repository.ProfileRepository;

import java.util.Optional;

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

    public boolean addContact(Long firstUserID, String secondUsername, String customName){
        ContactList contactList = new ContactList();
        Profile firstProfile = profileRepository.findById(firstUserID).get();
        Profile secondProfile = profileRepository.findByHandle(secondUsername).get();
        ContactID contactID = new ContactID(firstProfile, secondProfile);
        if(contactExists(contactID) != null)
            return false;
        contactList.setFirstUser(firstProfile);
        contactList.setSecondUser(secondProfile);
        contactList.setCustomName(customName);
        contactRepository.save(contactList);
        return true;
    }
}
