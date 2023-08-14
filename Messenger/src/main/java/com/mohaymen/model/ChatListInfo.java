package com.mohaymen.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@JsonView(Views.ChatDisplay.class)
@AllArgsConstructor
@Getter
public class ChatListInfo {
    private List<ChatDisplay> chatDisplayList;
    private boolean isFinished;

}
