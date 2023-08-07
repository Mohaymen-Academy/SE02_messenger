package com.mohaymen.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

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

    @Column(name = "biography")
    private String biography;

    @Column(name = "member_count")
    private Integer memberCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_type", nullable = false)
    private ChatType type;
}
