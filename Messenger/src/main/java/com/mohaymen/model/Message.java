package com.mohaymen.model;


import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@NoArgsConstructor
@Entity
@Table(name = "Message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long messageID;

    @Column(name = "text")
    private String text;

    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    //not sure about one to one or many to one
    @OneToOne
    @JoinColumn(name = "fk_media_id", referencedColumnName = "media_id")
    private MediaFile media;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @ManyToOne
    @JoinColumn(name = "fk_sender", referencedColumnName = "profile_id", nullable = false)
    private Profile sender;

    @ManyToOne
    @JoinColumn(name = "fk_receiver", referencedColumnName = "profile_id", nullable = false)
    private Profile receiver;

    @ManyToOne
    @JoinColumn(name = "fk_reply_message_id", referencedColumnName = "message_id")
    private Message replyMessageParent;

    @ManyToOne
    @JoinColumn(name = "fk_forward_message_id", referencedColumnName = "message_id")
    private Message forwardMessageParent;

    @Column(name = "is_pinned")
    private boolean isPinned;

    @Column(name = "is_edited")
    private boolean isEdited;

}
