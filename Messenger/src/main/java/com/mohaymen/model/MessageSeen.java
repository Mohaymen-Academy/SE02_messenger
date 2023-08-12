package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@IdClass(ProfilePareId.class)
@Table(name = "MESSAGE_SEEN")
public class MessageSeen {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_1", referencedColumnName = "profile_id")
    private Profile user;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_2", referencedColumnName = "profile_id")
    private Profile destination;

    @Setter
    @Column(name = "last_message_seen_id", nullable = false)
    private Long lastMessageSeenId;
}
