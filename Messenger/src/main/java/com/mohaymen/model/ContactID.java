package com.mohaymen.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ContactID implements Serializable {
    private Profile firstUser;
    private Profile secondUser;
}
