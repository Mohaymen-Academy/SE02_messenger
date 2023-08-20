package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import com.mohaymen.service.ProfileService;
import com.mohaymen.service.ServerService;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonView(Views.GetMessage.class)
@Getter
public class MessageDisplay {

    private final boolean isUpFinished;

    private final boolean isDownFinished;

    private final List<Message> messages;

    private final Long messageId;

    public MessageDisplay(List<Message> upMessages,
                          List<Message> downMessages,
                          Message message,
                          boolean isDownFinished,
                          boolean isUpFinished,
                          Profile serverProfile) {
        this.messages = new ArrayList<>();
        upMessages.forEach(m -> addMessage(m, serverProfile));
        addMessage(message, serverProfile);
        downMessages.forEach(m -> addMessage(m, serverProfile));
        this.isDownFinished = isDownFinished;
        this.isUpFinished = isUpFinished;
        this.messageId = message != null ? message.getMessageID() : 0;
    }

    private void addMessage(Message message, Profile serverProfile) {
        if (message == null)
            return;
        if (messages.size() == 0) {
            messages.add(message);
            return;
        }
        Message lastMessage = messages.get(messages.size() - 1);
        if(lastMessage.getTime().toLocalDate() != message.getTime().toLocalDate()){
            Message serverMessage = new Message();
            serverMessage.setMessageID(0L);
            serverMessage.setText(message.getTime().toLocalDate().toString());
            serverMessage.setTime(message.getTime());
            serverMessage.setSender(serverProfile);
            serverMessage.setReceiver(message.getReceiver());
            serverMessage.setTextStyle("");
            serverMessage.setViewCount(0);
            messages.add(serverMessage);
            messages.add(message);
        }
    }

}
