package spotifyPlaylist.playlist.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddSongRequestDto {
    private String artistName;
    private String title;
    private Long imageIdx;
    private String message;
}