package com.mohaymen.repository;

import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.model.supplies.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Profile> {

    Optional<Account> findByEmail(String email);

    Optional<Account>findById(Long id);

    Optional<Account> findByProfile(Profile profile);
//    @Transactional
//    @Query(value = "INSERT INTO Account (id,profile, password,email,status, last_seen,salt)" +
//            " VALUES (:id,:profile, :password, :email,:status, :last_seen,:salt) RETURNING *",nativeQuery = true)
//    Account insertAccount(@Param("id") Long profile_id,
//                          @Param("profile")Profile profile,
//                          @Param("password") byte[] password,
//                          @Param("email") String email,
//                          @Param("status")Status status,
//                          @Param("last_seen")LocalDateTime last_seen,
//                          @Param("salt") byte[] salt
//    );
}
