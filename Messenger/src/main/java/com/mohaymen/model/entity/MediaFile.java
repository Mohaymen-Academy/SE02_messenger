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

    @JsonView({Views.GetCompressedPicture.class,
            Views.GetOriginalPicture.class, Views.GetMessage.class,
            Views.GetMedia.class})
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @NotEmpty
    @Column(name = "media_name", nullable = false)
    private String mediaName;

    @Lob
    @JsonView({Views.GetOriginalPicture.class, Views.ProfileInfo.class, Views.GetMedia.class})
    @Column(name = "content", nullable = false)
    private byte[] content;

    @NotEmpty
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "content_size", nullable = false)
    private double contentSize;

    @Lob
    @JsonView({Views.ProfileLoginInfo.class , Views.GetCompressedPicture.class})
    @Column(name = "compressed_content")
    private byte[] compressedContent;

    @Lob
    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class, Views.MemberInfo.class})
    @Column(name = "preloading_content")
    private byte[] preLoadingContent;

}
