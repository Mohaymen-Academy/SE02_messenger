package com.mohaymen.model.supplies;

import com.mohaymen.model.entity.MediaFile;
import com.mohaymen.model.entity.Profile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
public class ProfilePictureID implements Serializable {

    private Profile profile;
    private MediaFile mediaFile;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfilePictureID that = (ProfilePictureID) o;
        return Objects.equals(getProfile().getProfileID(), that.getProfile().getProfileID()) &&
                Objects.equals(getMediaFile().getMediaId(), that.getMediaFile().getMediaId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProfile(), getMediaFile());
    }
}
