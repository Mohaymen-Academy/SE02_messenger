package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonView(Views.ProfileInfo.class)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileInfo {

    public Profile profile;

    public boolean isContact;

    public List<MediaFile> profilePictures;

}
