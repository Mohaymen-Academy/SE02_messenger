package com.mohaymen.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ProfilePictureID implements Serializable {

    private Profile profile;
    private MediaFile mediaFile;
}
