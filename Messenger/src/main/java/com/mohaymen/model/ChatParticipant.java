package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Chat_participant")
public class ChatParticipant {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_1", referencedColumnName = "profile_id")
    private Profile user;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_2", referencedColumnName = "profile_id")
    private Profile destination;

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin;

    @ManyToOne
    @JoinColumn(name = "fk_last_message_seen", referencedColumnName = "message_id")
    private Message lastMessageSeen;
}
