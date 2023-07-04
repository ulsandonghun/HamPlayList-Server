package spotifyPlaylist.playlist.domain;

import spotifyPlaylist.user.domain.User;
import lombok.Getter;
import lombok.Setter;
import spotifyPlaylist.user.domain.UserPlaylist;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playlistId;

    private String playlistName; // ex: 비오는 날 듣기 좋은 노래


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id")
    private User user;

    private String spotifyPlaylistId;

    private Long backgroundIdx; // 페이지(플레이리스트) 배경 테마

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPlaylist> userPlaylists = new ArrayList<>();
}