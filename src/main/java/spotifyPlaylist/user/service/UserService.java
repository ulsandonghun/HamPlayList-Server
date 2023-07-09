package spotifyPlaylist.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spotifyPlaylist.playlist.domain.PlaylistSong;
import spotifyPlaylist.playlist.repository.PlaylistSongRepository;
import spotifyPlaylist.user.domain.Follow;
import spotifyPlaylist.user.domain.Role;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.dto.*;
import spotifyPlaylist.user.repository.FollowRepository;
import spotifyPlaylist.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final FollowRepository followRepository;

    private final PlaylistSongRepository playlistSongRepository;

    public void signUp(UserSignUpDto userSignUpDto) throws Exception {

        if (userRepository.findByEmail(userSignUpDto.getEmail()).isPresent()) {
            throw new Exception("이미 존재하는 이메일입니다.");
        }

        if (userRepository.findByNickname(userSignUpDto.getNickname()).isPresent()) {
            throw new Exception("이미 존재하는 닉네임입니다.");
        }

        User user = User.builder()
                .email(userSignUpDto.getEmail())
                .password(userSignUpDto.getPassword())
                .nickname(userSignUpDto.getNickname())
                .role(Role.USER)
                .build();

        user.passwordEncode(passwordEncoder);
        userRepository.save(user);
    }

    @Transactional
    public void updateDescription(Long userId, UpdateDescriptionRequestDto updateDescriptionRequestDto) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setNickname(updateDescriptionRequestDto.getNickname());
            user.get().setOneLineIntroduction(updateDescriptionRequestDto.getIntroduce());
        } else {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
    }

    public void followOrUnfollow(Long userId, Long followerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower not found with id: " + followerId));

        Follow follow = followRepository.findByFollowerAndFollowing(follower, user);

        if (follow != null) {
            followRepository.deleteByFollowerAndFollowing(follower, user);
        } else {
            Follow newFollow = new Follow();
            newFollow.setFollower(follower);
            newFollow.setFollowing(user);
            followRepository.save(newFollow);
        }
    }

    public List<UserDto> getMyFriends(Long userId) {
        List<Follow> follows = followRepository.findByFollowingUserId(userId);
        return follows.stream()
                .map(follow -> {
                    UserDto userDto = new UserDto();
                    userDto.setId(follow.getFollower().getUserId());
                    userDto.setNickname(follow.getFollower().getNickname());
                    userDto.setIntroduce(follow.getFollower().getOneLineIntroduction());
                    return userDto;
                })
                .collect(Collectors.toList());
    }

    public UserSettingsDto getUserSettings(Long userId){ // 설정 화면 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        UserSettingsDto userSettingResponseDto = new UserSettingsDto();
        userSettingResponseDto.setNickname(user.getNickname());
        userSettingResponseDto.setIntroduce(user.getOneLineIntroduction());
        return userSettingResponseDto;
    }

    public UserSettingsDto updateUserSettings(Long userId, UserSettingsDto userSettingUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setNickname(userSettingUpdateDto.getNickname());
        user.setOneLineIntroduction(userSettingUpdateDto.getIntroduce());
        userRepository.save(user);

        UserSettingsDto updatedSettings = new UserSettingsDto();
        updatedSettings.setNickname(user.getNickname());
        updatedSettings.setIntroduce(user.getOneLineIntroduction());
        return updatedSettings;
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Delete all PlaylistSong entities that reference the User
        List<PlaylistSong> playlistSongs = playlistSongRepository.findByUser(user);
        playlistSongRepository.deleteAll(playlistSongs);

        userRepository.delete(user);
    }

    public SocialLoginResponseDto getUserInfo(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            SocialLoginResponseDto socialLoginResponseDto = new SocialLoginResponseDto();
            socialLoginResponseDto.setNickname(user.getNickname());
            socialLoginResponseDto.setIntroduce(user.getOneLineIntroduction());
            socialLoginResponseDto.setUserId(user.getUserId());

            return socialLoginResponseDto;
        } else {
            throw new RuntimeException("User not found");
        }
    }

}