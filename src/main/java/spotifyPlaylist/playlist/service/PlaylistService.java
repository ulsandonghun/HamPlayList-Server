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
    public void createPlaylist(Long userId, CreatePlaylistRequestDto createPlaylistRequestDto) { // 페이지(플레이리스트) 생성
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        Playlist playlist = new Playlist();
        playlist.setUser(user.get());
        playlist.setPlaylistName(createPlaylistRequestDto.getPlaylistName());
        playlist.setBackgroundIdx(createPlaylistRequestDto.getBackgroundIdx());
        playlistRepository.save(playlist);
    }

    @Transactional
    public void addSong(AddSongRequestDto addSongRequestDto, Long playlistId, Long userId) { // 페이지에 곡(스티커) 추가
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
            sticker.setImgIdx(addSongRequestDto.getImageIdx());
            sticker.setMessage(addSongRequestDto.getMessage());
            stickerRepository.save(sticker);
        }
    }

    public PlaylistResponseDto getUserWithPlaylists(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        List<Playlist> playlists = playlistRepository.findByUser(user);
        List<PlaylistInfoDto> playlistInfoDtos = playlists.stream().map(playlist -> {
            PlaylistInfoDto playlistInfoDto = new PlaylistInfoDto();
            playlistInfoDto.setPlaylistName(playlist.getPlaylistName());
            playlistInfoDto.setBackgroundIdx(playlist.getBackgroundIdx());
            return playlistInfoDto;
        }).collect(Collectors.toList());

        PlaylistResponseDto userPlaylistResponseDto = new PlaylistResponseDto();
        userPlaylistResponseDto.setIntroduce(user.getOneLineIntroduction());
        userPlaylistResponseDto.setNickname(user.getNickname());
        userPlaylistResponseDto.setPlaylists(playlistInfoDtos);
        return userPlaylistResponseDto;
    }

    public PlaylistDto getPlaylistWithSongsAndStickers(Long playlistId) { // 플레이리스트 곡 조회
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with id: " + playlistId));
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylist(playlist);

        PlaylistDto playlistDto = new PlaylistDto();
        playlistDto.setUserId(playlist.getUser().getUserId());
        playlistDto.setNickname(playlist.getUser().getNickname());
        playlistDto.setPlaylistId(playlist.getPlaylistId());
        playlistDto.setPlaylistName(playlist.getPlaylistName());
        playlistDto.setBackgroundIdx(playlist.getBackgroundIdx());

        List<PlaylistSongDto> playlistSongDtos = playlistSongs.stream().map(playlistSong -> {
            PlaylistSongDto playlistSongDto = new PlaylistSongDto();
            playlistSongDto.setPlaylistSongId(playlistSong.getPlaylistSongId());

            StickerDto stickerDto = new StickerDto();
            Optional<Sticker> optionalSticker = stickerRepository.findByPlaylistSong(playlistSong);
            if (optionalSticker.isPresent()) {
                Sticker sticker = optionalSticker.get();
                stickerDto.setStickerId(sticker.getStickerId());
                stickerDto.setImgIdx(sticker.getImgIdx());
            } else {
                // Sticker가 없는 경우 빈 StickerDto를 사용
                stickerDto.setStickerId(null);
                stickerDto.setImgIdx(null);
            }

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

    @javax.transaction.Transactional
    public void deletePlaylist(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with id: " + playlistId));
        playlistRepository.delete(playlist);
    }

}