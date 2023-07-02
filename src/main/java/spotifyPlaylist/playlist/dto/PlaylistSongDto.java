package spotifyPlaylist.playlist.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaylistSongDto {
    private Long playlistSongId;
    private List<StickerDto> stickers;
}
