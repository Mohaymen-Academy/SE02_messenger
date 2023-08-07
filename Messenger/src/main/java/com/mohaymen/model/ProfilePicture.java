package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Profile_picture")
public class ProfilePicture {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_id", referencedColumnName = "profile_id")
    private Profile profile ;

    @Id
    @OneToOne
    @JoinColumn(name = "fk_media_id", referencedColumnName = "media_id")
    private MediaFile mediaFile ;
}
