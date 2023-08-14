package com.mohaymen.repository;

import com.mohaymen.model.supplies.ContactID;
import com.mohaymen.model.entity.ContactList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<ContactList, ContactID> {

    List<ContactList> findByFirstUser_ProfileID(Long firstUser_profileID);

}
