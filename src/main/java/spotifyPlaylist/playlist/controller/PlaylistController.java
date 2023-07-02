package spotifyPlaylist.playlist.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.playlist.dto.AddSongRequestDto;
import spotifyPlaylist.playlist.dto.CreatePlaylistRequestDto;
import spotifyPlaylist.playlist.dto.PlaylistDto;
import spotifyPlaylist.playlist.dto.PlaylistResponseDto;
import spotifyPlaylist.playlist.service.PlaylistService;

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

    @PostMapping("/page/{userId}")
    public Playlist createPlaylist(@PathVariable Long userId, @RequestBody CreatePlaylistRequestDto createPlaylistRequestDto) {
        System.out.println("/page/{userId} test 호출");
        return playlistService.createPlaylist(userId, createPlaylistRequestDto.getPlaylistName());
    }

    @PostMapping("/{playlistId}/songs/{userId}")
    public void addSong(@PathVariable Long playlistId, @PathVariable Long userId, @RequestBody AddSongRequestDto addSongRequestDto) {
        playlistService.addSong(addSongRequestDto, playlistId, userId);
    }

    @GetMapping("/{userId}")
    public PlaylistResponseDto getPlaylists(@PathVariable Long userId) {
        return playlistService.getUserWithPlaylists(userId);
    }

    @GetMapping("/playlist/{playlistId}")
    public PlaylistDto getPlaylistWithSongsAndStickers(@PathVariable Long playlistId) {
        return playlistService.getPlaylistWithSongsAndStickers(playlistId);
    }

    @DeleteMapping("/{playlistId}/{playlistSongId}")
    public ResponseEntity<Void> deleteSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long playlistSongId) {
        playlistService.deleteSongFromPlaylist(playlistId, playlistSongId);
        return ResponseEntity.noContent().build();
    }

}
