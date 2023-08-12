package com.mohaymen.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@JsonView(Views.ChatDisplay.class)
@Getter
@Builder
@AllArgsConstructor
public class ChatDisplay {

    private Profile profile;

    private Message lastMessage;

    private int unreadMessageCount;

}
