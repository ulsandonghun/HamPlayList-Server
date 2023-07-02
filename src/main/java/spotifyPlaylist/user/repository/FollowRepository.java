package spotifyPlaylist.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spotifyPlaylist.user.domain.Follow;
import spotifyPlaylist.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Follow findByFollowerAndFollowing(User follower, User following);
    void deleteByFollowerAndFollowing(User follower, User following);
    List<Follow> findByFollowingUserId(Long userId);
}