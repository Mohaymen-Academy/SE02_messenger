package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import lombok.Getter;
import java.util.ArrayList;
import java.util.Date;
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
        this.isDownFinished = isDownFinished;
        this.isUpFinished = isUpFinished;
        this.messages = new ArrayList<>();
        for (int i = upMessages.size() - 1; i >= 0; i--) {
            addMessage(upMessages.get(i), serverProfile, this.isUpFinished);
        }
        addMessage(message, serverProfile, this.isUpFinished);
        downMessages.forEach(m -> addMessage(m, serverProfile, this.isUpFinished));
        this.messageId = message != null ? message.getMessageID() : 0;
    }

    private void addMessage(Message message, Profile serverProfile, Boolean isUpFinished) {
        if (message == null)
            return;
        if (messages.size() == 0) {
            if(isUpFinished)
                messages.add(getServerMessage(message, serverProfile));
            messages.add(message);
            return;
        }
        Message lastMessage = messages.get(messages.size() - 1);
        if (!Date.from(lastMessage.getTime()).toString().equals(Date.from(message.getTime()).toString())) {
            messages.add(getServerMessage(message, serverProfile));
        }
        messages.add(message);
    }

    private Message getServerMessage(Message message, Profile serverProfile) {
        Message serverMessage = new Message();
        serverMessage.setMessageID(0L);
        serverMessage.setText(Date.from(message.getTime()).toString());
        serverMessage.setTime(message.getTime());
        serverMessage.setSender(serverProfile);
        serverMessage.setReceiver(message.getReceiver());
        serverMessage.setTextStyle("");
        serverMessage.setViewCount(0);
        return serverMessage;
    }

}
