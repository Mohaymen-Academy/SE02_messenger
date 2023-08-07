package com.mohaymen.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "text")
    private String text;


    @Column(name = "time", nullable = false)
    private LocalDateTime time;

    //not sure about one to one or many to one
    @OneToOne
    @JoinColumn(name = "fk_media_id")
    private MediaFile media;

    @Column(name = "view_count")
    private Integer viewCount;

    @ManyToOne
    @JoinColumn(name = "sender_profile_id")
    private Profile sender;

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
