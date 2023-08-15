package com.mohaymen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

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

    @Setter
//    @NotEmpty
    @Column(name = "is_pinned", columnDefinition = "boolean default false")
    private boolean isPinned;


    @ManyToOne
    @JoinColumn(name = "fk_last_message_seen", referencedColumnName = "message_id")
    private Message lastMessageSeen;
}
