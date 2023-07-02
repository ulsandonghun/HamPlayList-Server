package spotifyPlaylist.playlist.repository;

import spotifyPlaylist.playlist.domain.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
}
