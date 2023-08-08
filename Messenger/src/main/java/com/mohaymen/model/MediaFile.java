package com.mohaymen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MediaFile")
@NoArgsConstructor
public class MediaFile {

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
}
