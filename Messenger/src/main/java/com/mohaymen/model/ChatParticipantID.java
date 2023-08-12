package com.mohaymen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantID implements Serializable {

    private Profile user;
    private Profile destination;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatParticipantID chatParticipantID = (ChatParticipantID) o;
        return Objects.equals(getUser().getProfileID(), chatParticipantID.getUser().getProfileID()) && Objects.equals(getDestination().getProfileID(), chatParticipantID.getDestination().getProfileID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getDestination());
    }
}
