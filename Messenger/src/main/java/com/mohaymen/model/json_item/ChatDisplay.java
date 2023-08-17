package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Account;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
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

    private boolean isUpdated;
    private String lastSeen;

}
