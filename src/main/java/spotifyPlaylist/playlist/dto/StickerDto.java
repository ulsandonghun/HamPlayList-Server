package spotifyPlaylist.playlist.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StickerDto {
    private Long stickerId;
    private Long imgIdx;
    private String message;
}