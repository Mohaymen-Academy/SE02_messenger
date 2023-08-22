package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@JsonView(Views.ChatDisplay.class)
@Getter
@Builder
@AllArgsConstructor
public class SearchResultItem {

    private Profile profile;

    private String text;

    private Long message_id;

}
