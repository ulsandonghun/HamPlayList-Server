package spotifyPlaylist.playlist.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePlaylistRequestDto {
    private String playlistName;
    private Long backgroundIdx;
    private String type;
}