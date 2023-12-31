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
@AllArgsConstructor
@NoArgsConstructor
public class ContactID implements Serializable {

    private Profile firstUser;

    private Profile secondUser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactID contactID = (ContactID) o;
        return Objects.equals(getFirstUser().getProfileID(), contactID.getFirstUser().getProfileID()) && Objects.equals(getSecondUser().getProfileID(), contactID.getSecondUser().getProfileID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstUser(), getSecondUser());
    }

}
