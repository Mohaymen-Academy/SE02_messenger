package com.mohaymen.service;

import com.mohaymen.model.supplies.ContactID;
import com.mohaymen.model.entity.ContactList;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.repository.ContactRepository;
import com.mohaymen.repository.ProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public ContactList contactExists(ContactID contactID){
        Optional<ContactList> contactList = contactRepository.findById(contactID);
        return contactList.orElse(null);
    }

    public Profile addContact(Long firstUserID, Long secondUserId, String customName){
        ContactList contactList = new ContactList();
        Profile firstUser = profileRepository.findById(firstUserID).get();
        Profile secondUser = profileRepository.findById(secondUserId).get();
        ContactID contactID = new ContactID(firstUser, secondUser);
        if(contactExists(contactID) != null)
            return null;
        contactList.setFirstUser(firstUser);
        contactList.setSecondUser(secondUser);
        if(customName == null || customName.isEmpty())
            customName = secondUser.getProfileName();
        contactList.setCustomName(customName);
        contactRepository.save(contactList);

        return getProfileWithCustomName(firstUser, secondUser);
    }

    @Transactional
    public boolean deleteContact(Long firstUserID, Long contactId){
        Profile firstProfile = profileRepository.findById(firstUserID).get();
        Profile secondProfile = profileRepository.findById(contactId).get();
        ContactID contactID = new ContactID(firstProfile, secondProfile);
        ContactList contactList = contactExists(contactID);
        if(contactList == null)
            return false;
        contactRepository.delete(contactList);
        return true;
    }

    public void editCustomName(Long id, Long profileId, String customName) {
        Profile user = profileRepository.findById(id).get();
        Profile contact = profileRepository.findById(profileId).get();
        ContactID contactID = new ContactID(user, contact);
        ContactList contactList = contactExists(contactID);
        contactList.setCustomName(customName);
        contactRepository.save(contactList);
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
