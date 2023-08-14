package com.mohaymen.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Log")
@NoArgsConstructor
@RequiredArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @NonNull
    @Column(name = "log_time")
    private LocalDateTime logTime;

    @NonNull
    @Column(name = "logger")
    private String logger;

    @NonNull
    @Column(name = "level", length = 15)
    private String level;

    @NonNull
    @Column(name = "message")
    private String message;

}