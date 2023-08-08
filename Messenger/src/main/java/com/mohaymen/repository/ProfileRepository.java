package com.mohaymen.repository;

import com.mohaymen.model.Account;
import com.mohaymen.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByHandle(String handle);
}
