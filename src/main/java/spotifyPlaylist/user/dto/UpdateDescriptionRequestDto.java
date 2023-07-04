package spotifyPlaylist.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDescriptionRequestDto {
    private String nickname;
    private String introduce;
}