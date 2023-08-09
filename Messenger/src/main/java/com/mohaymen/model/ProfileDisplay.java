package com.mohaymen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.awt.*;

@Getter
@Builder
@AllArgsConstructor
public class ProfileDisplay {

    private Long profileId;

    private String name;

    private Color color;

    private int unreadMessageCount;

    private ChatType type;

}
