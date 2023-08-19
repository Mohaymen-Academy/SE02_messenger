package com.mohaymen.model.supplies;

import com.mohaymen.model.entity.Profile;
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
public class ProfilePareId implements Serializable {

    private Profile user;
    private Profile destination;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfilePareId profilePareId = (ProfilePareId) o;
        return Objects.equals(getUser().getProfileID(), profilePareId.getUser().getProfileID()) &&
                Objects.equals(getDestination().getProfileID(), profilePareId.getDestination().getProfileID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getDestination());
    }
}
