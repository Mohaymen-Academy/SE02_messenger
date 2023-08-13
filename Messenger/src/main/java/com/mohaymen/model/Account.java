package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "Account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @MapsId
    private Profile profile;

    @Column(name = "password", nullable = false)
    private byte[] password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "last_seen", nullable = true)
    private LocalDateTime lastSeen;

    @Column(name = "last_seen_setting")
    @ColumnDefault("false")
    private boolean lastSeenSetting;

    @Column(name = "salt", nullable = false)
    private byte[] salt;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "enabled")
    private boolean enabled;
}
