package spotifyPlaylist.playlist.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.playlist.dto.*;
import spotifyPlaylist.playlist.service.PlaylistService;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping("/test")
    public String test() {
        System.out.println("playlist controller test");
        return "playlist controller test";
    }

    @PostMapping("/page/{userId}") // 페이지(플레이리스트) 추가
    public void createPlaylist(@PathVariable Long userId, @RequestBody CreatePlaylistRequestDto createPlaylistRequestDto) {
        playlistService.createPlaylist(userId, createPlaylistRequestDto);
    }

    @PostMapping("/{playlistId}/songs/{userId}") // 페이지에 곡(스티커) 추가
    public void addSong(@PathVariable Long playlistId, @PathVariable Long userId, @RequestBody AddSongRequestDto addSongRequestDto) {
        playlistService.addSong(addSongRequestDto, playlistId, userId);
    }

    @GetMapping("/{userId}")
    public PlaylistResponseDto getPlaylists(@PathVariable Long userId) {
        return playlistService.getUserWithPlaylists(userId);
    }

    @GetMapping("/playlist/{playlistId}") // 플레이리스트 곡 조회
    public PlaylistDto getPlaylistWithSongsAndStickers(@PathVariable Long playlistId) {
        return playlistService.getPlaylistWithSongsAndStickers(playlistId);
    }

    @DeleteMapping("/{playlistId}/{playlistSongId}")
    public ResponseEntity<Void> deleteSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long playlistSongId) {
        playlistService.deleteSongFromPlaylist(playlistId, playlistSongId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(@PathVariable Long playlistId) {
        playlistService.deletePlaylist(playlistId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sticker/{playlistId}")
    //틀정 플레이리스트의 모든 스티커를 반환.
    public List<StickerDto> getStickers(@PathVariable Long playlistId) {


        return playlistService.getStickers(playlistId);
    }
    @GetMapping("/sticker/{playlistId}/{playlistSongId}")
    //특정 플레이리스트의 특정 곡에대한 모든 스티커 반환
    public StickerDto getStickersByPlaylistSong(@PathVariable Long playlistId, @PathVariable Long playlistSongId) {
        return playlistService.getStickerByplaylistsong(playlistId, playlistSongId);
    }

    @PostMapping("/{playlistId}/title/{userId}") // 페이지에 곡(스티커) 추가
    public void addSongbyTitle(@PathVariable Long playlistId, @PathVariable Long userId, @RequestBody AddSongRequestDto addSongRequestDto) {
        playlistService.addSongbyTitle(addSongRequestDto, playlistId, userId);
    }

}
