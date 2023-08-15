package com.mohaymen.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.Views;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Message")
public class Message {

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageID;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "text",columnDefinition = "TEXT")
    private String text;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    @JsonView(Views.GetMessage.class)
    @OneToOne
    @JoinColumn(name = "fk_media_id", referencedColumnName = "media_id")
    private MediaFile media;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "view_count", nullable = false, columnDefinition = "int default 0")
    private Integer viewCount;

    @JsonView(Views.GetMessage.class)
    @ManyToOne
    @JoinColumn(name = "fk_sender", referencedColumnName = "profile_id", nullable = false)
    private Profile sender;

    @ManyToOne
    @JoinColumn(name = "fk_receiver", referencedColumnName = "profile_id", nullable = false)
    private Profile receiver;

    @ManyToOne
    @JoinColumn(name = "fk_reply_message_id", referencedColumnName = "message_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Message replyMessage;

    @ManyToOne
    @JoinColumn(name = "fk_forward_message_id", referencedColumnName = "message_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Message forwardMessage;

    @JsonView(Views.GetMessage.class)
    @JsonProperty(value="isPinned")
    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @JsonView(Views.GetMessage.class)
    @JsonProperty(value="isEdited")
    @Column(name = "is_edited")
    private boolean isEdited;

    public Message addView() {
        this.viewCount++;
        return this;
    }

}
