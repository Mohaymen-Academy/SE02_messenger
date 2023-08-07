package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Account")
public class Account {

    @Id
    @OneToOne
    @JoinColumn(name = "fk_profile_id", referencedColumnName = "id")
    private Profile profile;

    @Column(name = "password", nullable = false)
    private byte[] password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "status")
    private Status status;

    @Column(name = "last_seen")
    private String lastSeen;

    @Column(name = "last_seen_setting")
    @ColumnDefault("false")
    private boolean lastSeenSetting;
}
