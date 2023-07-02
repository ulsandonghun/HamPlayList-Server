package spotifyPlaylist.playlist.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaylistDto {  // 플레이리스트 곡 조회
    private Long playlistId;
    private String playlistName;
    private Long backgroundIdx;
    private List<PlaylistSongDto> playlistSongs;
}
