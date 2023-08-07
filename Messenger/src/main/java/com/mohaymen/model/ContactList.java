package com.mohaymen.model;


import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ContactList")
@NoArgsConstructor
public class ContactList {
    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_id1", referencedColumnName = "profile_id")
    private Profile firstUser;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_id2", referencedColumnName = "profile_id")
    private Profile secondUser;
}
