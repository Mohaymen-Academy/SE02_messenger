package com.mohaymen.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ChatParticipantID implements Serializable {
    private Profile user;
    private Profile destination;
}
