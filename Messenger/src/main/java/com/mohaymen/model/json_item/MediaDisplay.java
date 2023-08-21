package com.mohaymen.model.json_item;

import com.mohaymen.model.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MediaDisplay {

    private List<Message> images;
    private List<Message> voices;
    private List<Message> musics;
    private List<Message> files;

}
