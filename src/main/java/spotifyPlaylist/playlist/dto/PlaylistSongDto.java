package spotifyPlaylist.playlist.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaylistSongDto {
    private List<StickerDto> stickers;
    private Long playlistSongId;
    private String title;
    private String artist;
    private String albumImageUrl;
}
