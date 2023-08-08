package com.mohaymen.repository;


import com.mohaymen.model.Account;
import com.mohaymen.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
@EnableJpaRepositories
public interface AccountRepository extends JpaRepository<Account, Long> {
}
