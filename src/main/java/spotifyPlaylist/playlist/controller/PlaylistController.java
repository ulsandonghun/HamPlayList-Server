package spotifyPlaylist.playlist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.playlist.domain.PlaylistSong;
import spotifyPlaylist.playlist.dto.AddSongRequestDto;
import spotifyPlaylist.playlist.dto.CreatePlaylistRequestDto;
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
}
