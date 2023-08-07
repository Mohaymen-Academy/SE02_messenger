package com.mohaymen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "Profile")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long id;

    @NotEmpty
    @Column(name = "handle", unique = true, nullable = false)
    private String handle;

    @NotEmpty
    @Column(name = "profile_name", length = 255, nullable = false)
    private String profileName;

    @Column(name = "bio")
    private String biography;

    @Column(name = "user_count")
    private Integer memberCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChatType type;
}
