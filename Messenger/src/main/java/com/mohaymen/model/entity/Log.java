package com.mohaymen.model.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "Log")
@NoArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "log_time")
    private LocalDateTime logTime;

    @Column(name = "logger")
    private String logger;

    @Column(name = "level", length = 15)
    private String name;

    @Column(name = "message")
    private String message;

}