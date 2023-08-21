package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MediaDisplay {

    @JsonView(Views.GetMedia.class)
    private List<Message> images;

    @JsonView(Views.GetMedia.class)
    private List<Message> voices;

    @JsonView(Views.GetMedia.class)
    private List<Message> musics;

    @JsonView(Views.GetMedia.class)
    private List<Message> files;

}
