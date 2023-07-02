package spotifyPlaylist.playlist.repository;

import spotifyPlaylist.playlist.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import spotifyPlaylist.user.domain.User;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);
}
