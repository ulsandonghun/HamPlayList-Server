package spotifyPlaylist.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SearchResponseDto {
    private String artistName;
    private String title;
    private String albumName;
    private String imageUrl;
}
