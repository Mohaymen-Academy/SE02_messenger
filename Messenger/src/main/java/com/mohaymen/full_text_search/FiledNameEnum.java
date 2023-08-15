package com.mohaymen.full_text_search;

public enum FiledNameEnum {

    ProfileId("profile_id"),
    SenderId("sender_profile_id"),
    ReceiverId("receiver_profile_id"),
    MessageId("message_id"),
    Name("name"),
    MessageText("message_text"),
    Email("email"),
    Handle("handle");

    public final String value;

    FiledNameEnum(String value) {
        this.value = value;
    }

}
