package com.mohaymen.model.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.json_item.Views;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MediaFile")
@Getter
@Setter
@NoArgsConstructor
public class MediaFile {

    @JsonView(Views.getCompressedProfilePicture.class)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @NotEmpty
    @Column(name = "media_name", nullable = false)
    private String mediaName;

    //is there an empty file or we don't support it?
    @Column(name = "content", nullable = false)
    private byte[] content;

    @NotEmpty
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "content_size", nullable = false)
    private double contentSize;

    @JsonView({Views.ProfileLoginInfo.class , Views.getCompressedProfilePicture.class})
    @Column(name = "compressed_content")
    private byte[] compressedContent;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class, Views.ProfileLoginInfo.class})
    @Column(name = "preloading_content")
    private byte[] preLoadingContent;
}
