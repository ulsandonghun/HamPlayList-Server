package SpotifyPlaylist.playlist.domain;

import SpotifyPlaylist.user.domain.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class PlaylistSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String spotifyId;

    private String title;

    private String artist;

    private String album;

    private int durationMs;

    private String imageUrl;

}
