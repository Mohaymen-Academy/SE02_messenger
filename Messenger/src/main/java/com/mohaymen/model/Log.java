package com.mohaymen.model;


import jakarta.persistence.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Table(name = "Log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "log_time")
    private LocalDateTime logTime;

    @Column(name = "sender")
    private String sender;

    @Column(name = "type")
    private String type;

    @Column(name = "message")
    private String message;
}
