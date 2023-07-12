package spotifyPlaylist.playlist.repository;

import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.playlist.domain.PlaylistSong;
import org.springframework.data.jpa.repository.JpaRepository;
import spotifyPlaylist.user.domain.User;

import java.util.List;

public interface PlaylistSongRepository extends JpaRepository<PlaylistSong, Long> {
    List<PlaylistSong> findByPlaylist(Playlist playlist);
    void deleteByUser(User user);
}
