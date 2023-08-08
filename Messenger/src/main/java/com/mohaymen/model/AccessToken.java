package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
public class AccessToken {

    @Id
    @Column(name = "token", nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "fk_profile_id", referencedColumnName = "profile_id")
    private Profile user;

    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    //system
}
