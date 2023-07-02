package spotifyPlaylist.user.domain;

import spotifyPlaylist.playlist.domain.Playlist;

import javax.persistence.*;

@Entity
public class UserPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

}