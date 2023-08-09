package com.mohaymen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileDisplay {

    private Long profileId;

    private String name;

    private byte[] image;

    int unreadMessageCount;

}
