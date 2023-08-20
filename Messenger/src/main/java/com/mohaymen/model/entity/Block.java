package com.mohaymen.model.entity;

import com.mohaymen.model.supplies.ProfilePareId;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "BlockTable")
@IdClass(ProfilePareId.class)
@NoArgsConstructor
@RequiredArgsConstructor
public class Block {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_blocker", referencedColumnName = "profile_id")
    @NonNull
    private Profile user;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_blocked", referencedColumnName = "profile_id")
    @NonNull
    private Profile destination;

}
