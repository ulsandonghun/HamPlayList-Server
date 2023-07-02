package spotifyPlaylist.playlist.repository;

import spotifyPlaylist.playlist.domain.PlaylistSong;
import spotifyPlaylist.playlist.domain.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
    Optional<Sticker> findByPlaylistSong(PlaylistSong playlistSong);
}
