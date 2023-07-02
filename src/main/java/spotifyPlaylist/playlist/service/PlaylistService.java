package spotifyPlaylist.playlist.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.playlist.domain.PlaylistSong;
import spotifyPlaylist.playlist.domain.Sticker;
import spotifyPlaylist.playlist.dto.AddSongRequestDto;
import spotifyPlaylist.playlist.repository.PlaylistRepository;
import spotifyPlaylist.playlist.repository.PlaylistSongRepository;
import spotifyPlaylist.playlist.repository.StickerRepository;
import spotifyPlaylist.service.SpotifyService;
import spotifyPlaylist.dto.SearchResponseDto;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    private final SpotifyService spotifyService;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final PlaylistSongRepository playlistSongRepository;
    private final StickerRepository stickerRepository;

    private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);

    public PlaylistService(SpotifyService spotifyService, PlaylistRepository playlistRepository, UserRepository userRepository, PlaylistSongRepository playlistSongRepository, StickerRepository stickerRepository) {
        this.spotifyService = spotifyService;
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.playlistSongRepository = playlistSongRepository;
        this.stickerRepository = stickerRepository;
    }

    @Transactional
    public Playlist createPlaylist(Long userId, String playlistName) {
        System.out.println("createPlaylist시작 ");
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        System.out.println("createPlaylist테스트 " + user.get().getUserId());

        Playlist playlist = new Playlist();
        playlist.setUser(user.get());
        playlist.setPlaylistName(playlistName);
        return playlistRepository.save(playlist);
    }

    @Transactional
    public void addSong(AddSongRequestDto addSongRequestDto, Long playlistId, Long userId) {
        List<SearchResponseDto> searchResults = spotifyService.SearchByTracknameAndArtist(addSongRequestDto.getTitle(), addSongRequestDto.getArtistName());

        if (!searchResults.isEmpty()) {
            SearchResponseDto searchResult = searchResults.get(0);

            PlaylistSong playlistSong = new PlaylistSong();
            playlistSong.setTitle(searchResult.getTitle());
            playlistSong.setArtist(searchResult.getArtistName());
            playlistSong.setAlbumImageUrl(searchResult.getImageUrl());

            Optional<User> user = userRepository.findById(userId);
            user.ifPresent(playlistSong::setUser);

            Optional<Playlist> playlist = playlistRepository.findById(playlistId);
            playlist.ifPresent(playlistSong::setPlaylist);

            playlistSong = playlistSongRepository.save(playlistSong);

            Sticker sticker = new Sticker();
            sticker.setPlaylistSong(playlistSong);
            sticker.setImgUrl(addSongRequestDto.getImageUrl());
            stickerRepository.save(sticker);

            // Print the state of the Sticker entity
            log.info("Sticker: " + sticker);
        }
    }
}