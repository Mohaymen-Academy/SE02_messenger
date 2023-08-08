package com.mohaymen.repository;

import com.mohaymen.model.Account;
import com.mohaymen.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Profile> {
}
