package com.mohaymen.model.json_item;

import com.fasterxml.jackson.annotation.JsonView;
import com.mohaymen.model.entity.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@JsonView(Views.ProfileInfo.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileInfo {

    public Profile profile;

    Boolean isContact;

}
