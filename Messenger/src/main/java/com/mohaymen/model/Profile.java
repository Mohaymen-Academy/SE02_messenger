package com.mohaymen.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Profile")
public class Profile {

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileID;

    @NotEmpty
    @Column(name = "handle")
    private String handle;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @NotEmpty
    @Column(name = "profile_name", length = 50, nullable = false)
    private String profileName;

    @Column(name = "bio")
    private String biography;

    @Column(name = "user_count")
    private Integer memberCount;

    @JsonView(Views.ChatDisplay.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChatType type;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @Column(name = "default_profile_color")
    private String defaultProfileColor;

    @JsonView({Views.GetMessage.class, Views.ChatDisplay.class})
    @OneToOne
    @JoinColumn(name = "fk_mediaFile_id", referencedColumnName = "media_id")
    private MediaFile lastProfilePicture;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}
