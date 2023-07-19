package spotifyPlaylist.playlist.domain;

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

    private Long imgIdx; // 스티커 이미지 인덱스

    private String message; // 메시지

    private String imageUrl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_song_id")
    private PlaylistSong playlistSong;

}