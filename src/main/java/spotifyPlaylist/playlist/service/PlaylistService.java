package spotifyPlaylist.playlist.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.playlist.domain.PlaylistSong;
import spotifyPlaylist.playlist.domain.Sticker;
import spotifyPlaylist.playlist.dto.*;
import spotifyPlaylist.playlist.repository.PlaylistRepository;
import spotifyPlaylist.playlist.repository.PlaylistSongRepository;
import spotifyPlaylist.playlist.repository.StickerRepository;
import spotifyPlaylist.service.SpotifyService;
import spotifyPlaylist.dto.SearchResponseDto;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public PlaylistResponseDto getUserWithPlaylists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        List<Playlist> playlists = playlistRepository.findByUser(user);
        List<PlaylistInfoDto> playlistInfoDtos = playlists.stream().map(playlist -> {
            PlaylistInfoDto playlistInfoDto = new PlaylistInfoDto();
            playlistInfoDto.setPlaylistName(playlist.getPlaylistName());
            playlistInfoDto.setBackground(playlist.getBackground());
            return playlistInfoDto;
        }).collect(Collectors.toList());

        PlaylistResponseDto userPlaylistResponseDto = new PlaylistResponseDto();
        userPlaylistResponseDto.setOneLineIntroduction(user.getOneLineIntroduction());
        userPlaylistResponseDto.setNickname(user.getNickname());
        userPlaylistResponseDto.setPlaylists(playlistInfoDtos);
        return userPlaylistResponseDto;
    }

    public PlaylistDto getPlaylistWithSongsAndStickers(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with id: " + playlistId));
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylist(playlist);

        PlaylistDto playlistDto = new PlaylistDto();
        playlistDto.setPlaylistId(playlist.getPlaylistId());
        playlistDto.setPlaylistName(playlist.getPlaylistName());
        playlistDto.setBackground(playlist.getBackground());

        List<PlaylistSongDto> playlistSongDtos = playlistSongs.stream().map(playlistSong -> {
            PlaylistSongDto playlistSongDto = new PlaylistSongDto();
            playlistSongDto.setPlaylistSongId(playlistSong.getPlaylistSongId());

            Sticker sticker = stickerRepository.findByPlaylistSong(playlistSong)
                    .orElseThrow(() -> new IllegalArgumentException("Sticker not found for playlist song id: " + playlistSong.getPlaylistSongId()));
            StickerDto stickerDto = new StickerDto();
            stickerDto.setStickerId(sticker.getStickerId());
            stickerDto.setImgUrl(sticker.getImgUrl());

            playlistSongDto.setStickers(Collections.singletonList(stickerDto));
            return playlistSongDto;
        }).collect(Collectors.toList());

        playlistDto.setPlaylistSongs(playlistSongDtos);
        return playlistDto;
    }

    @Transactional
    public void deleteSongFromPlaylist(Long playlistId, Long playlistSongId) { // 플레이리스트 곡 삭제
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with id: " + playlistId));
        PlaylistSong playlistSong = playlistSongRepository.findById(playlistSongId)
                .orElseThrow(() -> new IllegalArgumentException("PlaylistSong not found with id: " + playlistSongId));
        if (!playlistSong.getPlaylist().equals(playlist)) {
            throw new IllegalArgumentException("PlaylistSong with id: " + playlistSongId + " is not in the Playlist with id: " + playlistId);
        }

        // Delete the Sticker entity that references the PlaylistSong
        Sticker sticker = stickerRepository.findByPlaylistSong(playlistSong).orElse(null);
        if (sticker != null) {
            stickerRepository.delete(sticker);
        }

        playlistSongRepository.delete(playlistSong);
    }

}