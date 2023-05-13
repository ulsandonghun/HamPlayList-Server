package SpotifyPlaylist.playlist.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Sticker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stickerId;

    private String imgUrl; // 스티커 이미지 url

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_song_id")
    private PlaylistSong playlistSong;

}
