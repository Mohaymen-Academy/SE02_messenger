package com.mohaymen.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@IdClass(ChatParticipantID.class)
@Table(name = "Chat_participant")
public class ChatParticipant {

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_1", referencedColumnName = "profile_id")
    @NonNull
    private Profile user;

    @Id
    @ManyToOne
    @JoinColumn(name = "fk_profile_2", referencedColumnName = "profile_id")
    @NonNull
    private Profile destination;

    @Column(name = "is_admin", nullable = false)
    @NonNull
    private boolean isAdmin;

    @Column(name = "last_message_seen_id", nullable = false)
    private Long lastMessageSeenId;
}
