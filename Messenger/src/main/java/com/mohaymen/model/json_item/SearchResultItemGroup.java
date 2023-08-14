package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@JsonView(Views.ChatDisplay.class)
@Getter
@Builder
@AllArgsConstructor
public class SearchResultItemGroup {

    private String title;

    private List<SearchResultItem> items;

}
