package SpotifyPlaylist.user.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String kakaoRefreshToken;

    private String oneLineIntroduction;

    private String refreshToken;

    private String email;

    private String nickname;

    private String profileImage;

    private String spotifyRefreshToken;

}
