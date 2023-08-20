package com.mohaymen.model.entity;

import com.mohaymen.model.supplies.ContactID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Setter
@Getter
@IdClass(ContactID.class)
@Table(name = "ContactList")
public class ContactList {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_id1", referencedColumnName = "profile_id")
    private Profile firstUser;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_id2", referencedColumnName = "profile_id")
    private Profile secondUser;

    @Column(name = "custom_name")
    private String customName;

}
