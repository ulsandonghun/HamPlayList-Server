package spotifyPlaylist.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.domain.UserPlaylist;

import java.util.List;

public interface UserPlaylistRepository extends JpaRepository<UserPlaylist, Long>{
    List<UserPlaylist> findByUser(User user);
    List<UserPlaylist> findByPlaylist(Playlist playlist);
}
