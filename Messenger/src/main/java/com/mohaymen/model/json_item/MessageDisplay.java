package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
import com.mohaymen.model.entity.Profile;
import lombok.Getter;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@JsonView(Views.GetMessage.class)
@Getter
public class MessageDisplay {

    private final boolean isUpFinished;

    private final boolean isDownFinished;

    private final List<Message> messages;

    private final Long messageId;
    private final DateTimeFormatter formatter;

    public MessageDisplay(List<Message> upMessages,
                          List<Message> downMessages,
                          Message message,
                          boolean isDownFinished,
                          boolean isUpFinished,
                          Profile serverProfile) {
        formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd").withZone(ZoneId.of("Asia/Tehran"));
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
        if (!formatter.format(lastMessage.getTime()).equals(formatter.format(message.getTime()))) {
            messages.add(getServerMessage(message, serverProfile));
        }
        messages.add(message);
    }

    private Message getServerMessage(Message message, Profile serverProfile) {
        Message serverMessage = new Message();
        serverMessage.setMessageID(0L);
        serverMessage.setText(formatter.format(message.getTime()));
        serverMessage.setTime(message.getTime());
        serverMessage.setSender(serverProfile);
        serverMessage.setReceiver(message.getReceiver());
        serverMessage.setTextStyle("");
        serverMessage.setViewCount(0);
        return serverMessage;
    }

}
