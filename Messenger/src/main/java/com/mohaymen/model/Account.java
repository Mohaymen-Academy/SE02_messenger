package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Account")
public class Account {

    @Id
    @OneToOne
    @JoinColumn(name = "fk_profile_id", referencedColumnName = "profile_id")
    private Profile profile;

    @Column(name = "password", nullable = false)
    private byte[] password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "last_seen",nullable = false)
    private LocalDateTime lastSeen;

    @Column(name = "last_seen_setting")
    @ColumnDefault("false")
    private boolean lastSeenSetting;
}
