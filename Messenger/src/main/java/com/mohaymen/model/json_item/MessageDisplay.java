package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
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
                          boolean isUpFinished) {
        this.messages = new ArrayList<>();
        addUpMessages(upMessages);
        addMessage(message);
        addDownMessages(downMessages);
        this.isDownFinished = isDownFinished;
        this.isUpFinished = isUpFinished;
        this.messageId = message != null? message.getMessageID() : 0;
    }

    private void addUpMessages(List<Message> upMessages) {
        Collections.reverse(upMessages);
        this.messages.addAll(upMessages);
    }

    private void addMessage(Message message) {
        if (message != null)
            this.messages.add(message);
    }

    private void addDownMessages(List<Message> downMessages) {
        this.messages.addAll(downMessages);
    }

}
