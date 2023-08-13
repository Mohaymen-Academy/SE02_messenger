package com.mohaymen.model;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@JsonView(Views.ProfileLoginInfo.class)
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class LoginInfo {

    @NonNull
    public String message;

    public String jwt;

    public Profile profile;

}
