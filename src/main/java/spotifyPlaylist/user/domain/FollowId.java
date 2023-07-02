package spotifyPlaylist.user.domain;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FollowId implements Serializable {

    private Long follower;

    private Long following;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowId followId = (FollowId) o;
        return Objects.equals(follower, followId.follower) &&
                Objects.equals(following, followId.following);
    }

    @Override
    public int hashCode() {
        return Objects.hash(follower, following);
    }
}