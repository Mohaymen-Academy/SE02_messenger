package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@JsonView(Views.GetMessage.class)
@AllArgsConstructor
@Getter
public class MessageDisplay {

    private List<Message> upMessages;
    private Message message;
    private List<Message> downMessages;
    private boolean isUpFinished;
    private boolean isDownFinished;

}
