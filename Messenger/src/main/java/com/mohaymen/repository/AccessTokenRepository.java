package com.mohaymen.repository;

import com.mohaymen.model.AccessToken;
import jakarta.persistence.Access;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, String> {
    Optional<AccessToken> findByIp(String ip);
    void deleteByIp(String ip);
}
