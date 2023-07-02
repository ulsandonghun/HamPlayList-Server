package spotifyPlaylist.playlist.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaylistResponseDto {
    private String oneLineIntroduction;
    private String nickname;
    private List<PlaylistInfoDto> playlists;
}
