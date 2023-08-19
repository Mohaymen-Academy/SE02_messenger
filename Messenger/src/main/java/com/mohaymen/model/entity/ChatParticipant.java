package com.mohaymen.model.entity;

import com.mohaymen.model.supplies.ProfilePareId;
import jakarta.persistence.*;
import lombok.*;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@IdClass(ProfilePareId.class)
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

    @Setter
    @Column(name = "is_admin", nullable = false)
    @NonNull
    private boolean isAdmin;

    @Setter
    @Column(name = "is-updated", columnDefinition = "boolean default false")
    private boolean isUpdated;

    @Setter
    @Column(name = "is_pinned", columnDefinition = "boolean default false")
    private boolean isPinned;

}
