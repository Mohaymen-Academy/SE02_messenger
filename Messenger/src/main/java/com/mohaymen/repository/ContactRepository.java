package com.mohaymen.repository;

import com.mohaymen.model.ContactID;
import com.mohaymen.model.ContactList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<ContactList, ContactID> {
}
