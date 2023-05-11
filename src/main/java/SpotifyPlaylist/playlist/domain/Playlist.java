package SpotifyPlaylist.playlist.domain;

import SpotifyPlaylist.user.domain.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playlistName;

    private String Description;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String spotifyPlaylistId;

}
