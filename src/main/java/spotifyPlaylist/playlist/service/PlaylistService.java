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

import java.util.ArrayList;
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
        playlist.setType(createPlaylistRequestDto.getType());
        playlist.setImageIdx(createPlaylistRequestDto.getImageIdx());
        playlistRepository.save(playlist);
    }

    @Transactional
    public void addSong(AddSongRequestDto addSongRequestDto, Long playlistId, Long userId) { // 페이지에 곡(스티커) 추가



            PlaylistSong playlistSong = new PlaylistSong();
            playlistSong.setTitle(addSongRequestDto.getTitle());
            playlistSong.setArtist(addSongRequestDto.getArtistName());
            playlistSong.setAlbumImageUrl(addSongRequestDto.getImageUrl());

            Optional<User> user = userRepository.findById(userId);
            user.ifPresent(playlistSong::setUser);

            Optional<Playlist> playlist = playlistRepository.findById(playlistId);
            playlist.ifPresent(playlistSong::setPlaylist);

            playlistSong = playlistSongRepository.save(playlistSong);

            Sticker sticker = new Sticker();
            sticker.setPlaylistSong(playlistSong);
            sticker.setImgIdx(addSongRequestDto.getImageIdx());
            sticker.setMessage(addSongRequestDto.getMessage());
            sticker.setImageUrl(addSongRequestDto.getImageUrl());
            stickerRepository.save(sticker);

    }
    @Transactional
    //노래이름과 가수 함께 검색시, 가수이름이 영어로 스포티파이에 저장될 경우 검색 안됨.
    // 만약 가수이름이 스포티파이에 영어로만 저장되는 경우에 검색이 안됨, 조금더 제약조건 없이 노래이름으로만 검색 가능
    //노래이름은 띄어쓰기, 대소문자 영어, 한국어 무관하게 검색됨(rough) 또한 검색된 정보로 역으로
    public void addSongbyTitle(AddSongRequestDto addSongRequestDto, Long playlistId, Long userId) { // 페이지에 곡(스티커) 추가
        List<SearchResponseDto> searchResults = spotifyService.SearchByTrackname(addSongRequestDto.getTitle());

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
            playlistInfoDto.setUserIdx(playlist.getUser().getUserId());
            playlistInfoDto.setImageIdx(playlist.getImageIdx());
            playlistInfoDto.setType(playlist.getType());
            playlistInfoDto.setPlaylistIdx(playlist.getPlaylistId());
            return playlistInfoDto;
        }).collect(Collectors.toList());

        PlaylistResponseDto userPlaylistResponseDto = new PlaylistResponseDto();
        userPlaylistResponseDto.setIntroduce(user.getOneLineIntroduction());
        userPlaylistResponseDto.setNickname(user.getNickname());
        userPlaylistResponseDto.setPlaylists(playlistInfoDtos);
        return userPlaylistResponseDto;




    }


    public PlaylistDto getAllPlaylistWithSongsAndStickers(Long userId) { // 플레이리스트 곡 조회

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        List<Playlist> playlists = playlistRepository.findByUser(user);

        PlaylistDto playlistDto = new PlaylistDto();
        playlistDto.setUserId(user.getUserId());
        playlistDto.setNickname(user.getNickname());

        List<PlaylistSongDto> allPlaylistSongs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            playlistDto.setPlaylistId(playlist.getPlaylistId());
            playlistDto.setPlaylistName(playlist.getPlaylistName());
            playlistDto.setBackgroundIdx(playlist.getBackgroundIdx());

            List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylist(playlist);

            List<PlaylistSongDto> playlistSongDtos = playlistSongs.stream().map(playlistSong -> {
                PlaylistSongDto playlistSongDto = new PlaylistSongDto();
                playlistSongDto.setPlaylistSongId(playlistSong.getPlaylistSongId());
                playlistSongDto.setTitle(playlistSong.getTitle());
                playlistSongDto.setArtist(playlistSong.getArtist());
                playlistSongDto.setAlbumImageUrl(playlistSong.getAlbumImageUrl());

                StickerDto stickerDto = new StickerDto();
                stickerRepository.findByPlaylistSong(playlistSong).ifPresent(sticker -> {
                    stickerDto.setStickerId(sticker.getStickerId());
                    stickerDto.setImgIdx(sticker.getImgIdx());
                    stickerDto.setMessage(sticker.getMessage());
                });

                playlistSongDto.setStickers(Collections.singletonList(stickerDto));
                return playlistSongDto;
            }).collect(Collectors.toList());

            allPlaylistSongs.addAll(playlistSongDtos);
        }

        playlistDto.setPlaylistSongs(allPlaylistSongs);
        return playlistDto;
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
            playlistSongDto.setTitle(playlistSong.getTitle());
            playlistSongDto.setArtist(playlistSong.getArtist());
            playlistSongDto.setAlbumImageUrl(playlistSong.getAlbumImageUrl());

            StickerDto stickerDto = new StickerDto();
            stickerRepository.findByPlaylistSong(playlistSong).ifPresent(sticker -> {
                stickerDto.setStickerId(sticker.getStickerId());
                stickerDto.setImgIdx(sticker.getImgIdx());
                stickerDto.setMessage(sticker.getMessage());
            });

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

    public StickerDto getSticker(Long userId,Long playlistSongId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        PlaylistSong playlistSong = playlistSongRepository.findById(playlistSongId)
                .orElseThrow(() -> new IllegalArgumentException("PlaylistSong not found with id: " + playlistSongId));

        Sticker sticker = stickerRepository.findByPlaylistSong(playlistSong)
                .orElseThrow(() -> new IllegalArgumentException("Sticker not found for playlist song id: " + playlistSongId));

        StickerDto stickerDto = new StickerDto();
        stickerDto.setStickerId(sticker.getStickerId());
        stickerDto.setImgIdx(sticker.getImgIdx());

        return stickerDto;
    }


    public List<StickerDto> getStickers( Long playlistId) {
        //playlistsong과 무관하게 해당 playlistId에 있는 모든 스티커를 반환하는 함수.
        //즉, 해당 playlist의 모든 스티커를 반환.


        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with id: " + playlistId));

        List<PlaylistSong> playlistSongs = playlistSongRepository.findByPlaylist(playlist);

        List<StickerDto> stickerDtos = new ArrayList<>();
        for (PlaylistSong playlistSong : playlistSongs) {
            Optional<Sticker> stickerOptional = stickerRepository.findByPlaylistSong(playlistSong);
            stickerOptional.ifPresent(sticker -> {
                StickerDto stickerDto = new StickerDto();
                stickerDto.setStickerId(sticker.getStickerId());
                stickerDto.setImgIdx(sticker.getImgIdx());
                stickerDtos.add(stickerDto);
            });
        }

        return stickerDtos;
    }
    public StickerDto getStickerByplaylistsong(Long playlistId, Long playlistSongId) {
        //플레이리스트의 playlistSong별 스티커를 반환하는 함수.
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with id: " + playlistId));

        PlaylistSong playlistSong = playlistSongRepository.findById(playlistSongId)
                .orElseThrow(() -> new IllegalArgumentException("PlaylistSong not found with id: " + playlistSongId));

        if (!playlistSong.getPlaylist().equals(playlist)) {
            throw new IllegalArgumentException("PlaylistSong with id: " + playlistSongId + " is not in the Playlist with id: " + playlistId);
        }

        Sticker sticker = stickerRepository.findByPlaylistSong(playlistSong)
                .orElseThrow(() -> new IllegalArgumentException("Sticker not found for playlist song id: " + playlistSongId));

        StickerDto stickerDto = new StickerDto();
        stickerDto.setStickerId(sticker.getStickerId());
        stickerDto.setImgIdx(sticker.getImgIdx());

        return stickerDto;
    }

    @Transactional
    public void updatePlaylist(Long userId, Long playlistId, UpdatePlaylistRequestDto updatePlaylistRequestDto) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found with id: " + playlistId));

        // Update playlist properties
        playlist.setPlaylistName(updatePlaylistRequestDto.getPlaylistName());
        playlist.setBackgroundIdx((long) updatePlaylistRequestDto.getBackgroundIdx());

        playlistRepository.save(playlist);
    }

}