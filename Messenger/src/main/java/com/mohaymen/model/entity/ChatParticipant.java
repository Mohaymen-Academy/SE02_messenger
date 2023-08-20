package com.mohaymen.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.Views;
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
    @Column(name = "chat_id")
    @NonNull
    private String chatId;

    @Setter
    @Column(name = "is_admin", nullable = false)
    @NonNull
    private boolean isAdmin;

    @Setter
    @Column(name = "last-update")
    private Long lastUpdate;

    @Setter
    @Column(name = "is_pinned", columnDefinition = "boolean default false")
    private boolean isPinned;

    @JsonView(Views.GetMessage.class)
    @Setter
    @ManyToOne
    @JoinColumn(name = "pinned_msg",referencedColumnName = "message_id")
    private Message pinnedMessage;
}
