package com.mohaymen.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

@JsonView(Views.ChatDisplay.class)
public enum ChatType {
    USER(0),
    GROUP(1),
    CHANNEL(2);

    @JsonValue
    public final int value;

    private ChatType(int value) {
        this.value = value;
    }

}
