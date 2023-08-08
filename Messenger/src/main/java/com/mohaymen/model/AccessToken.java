package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AccessToken {


    @Id
    @Column(name = "token", nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "fk_profile_id", referencedColumnName = "profile_id")
    private Profile user;


    @Setter
    @Column(name = "expiration_time", nullable = false)
    private Date expirationTime;


    @Column(name = "ip", nullable = false)
    private String ip;
}
