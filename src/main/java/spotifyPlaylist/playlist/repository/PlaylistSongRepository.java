package spotifyPlaylist.playlist.repository;

import spotifyPlaylist.playlist.domain.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
}
