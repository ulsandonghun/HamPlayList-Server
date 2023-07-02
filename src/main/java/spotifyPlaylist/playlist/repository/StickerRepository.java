package spotifyPlaylist.playlist.repository;

import spotifyPlaylist.playlist.domain.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StickerRepository extends JpaRepository<Sticker, Long> {
}
