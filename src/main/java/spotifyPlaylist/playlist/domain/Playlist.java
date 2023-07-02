package spotifyPlaylist.playlist.domain;

import spotifyPlaylist.user.domain.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playlistId;

    private String playlistName; // ex: 비오는 날 듣기 좋은 노래


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String spotifyPlaylistId;

    private Long backgroundIdx; // 페이지(플레이리스트) 배경 테마
}