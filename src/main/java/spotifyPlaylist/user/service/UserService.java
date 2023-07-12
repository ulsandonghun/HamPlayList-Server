package spotifyPlaylist.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spotifyPlaylist.playlist.domain.Playlist;
import spotifyPlaylist.playlist.domain.PlaylistSong;
import spotifyPlaylist.playlist.repository.PlaylistRepository;
import spotifyPlaylist.playlist.repository.PlaylistSongRepository;
import spotifyPlaylist.user.domain.Follow;
import spotifyPlaylist.user.domain.Role;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.domain.UserPlaylist;
import spotifyPlaylist.user.dto.*;
import spotifyPlaylist.user.repository.FollowRepository;
import spotifyPlaylist.user.repository.UserPlaylistRepository;
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

    private final UserPlaylistRepository userPlaylistRepository;

    private final PlaylistRepository playlistRepository;

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
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));

        /**
        user.getUserPlaylists().size(); 이 코드는 User와 연관된 UserPlaylist 엔티티를 명시적으로 로딩하는 역할을 합니다. 이는 Hibernate의 지연 로딩(Lazy Loading) 전략 때문에 필요한 코드입니다.

        Hibernate는 성능 최적화를 위해 기본적으로 지연 로딩 전략을 사용합니다. 즉, 특정 엔티티를 조회할 때 연관된 엔티티를 즉시 로딩하지 않고, 실제로 사용될 때까지 로딩을 지연시키는 것입니다. 이 경우 User 엔티티를 조회할 때 연관된 UserPlaylist 엔티티는 로딩되지 않습니다.

        따라서 User를 삭제하기 전에 UserPlaylist를 명시적으로 로딩해야 합니다. 그렇지 않으면 User를 삭제하려고 할 때 UserPlaylist가 아직 로딩되지 않았으므로 데이터 무결성 제약 조건(Data Integrity Constraint)이 위반되어 오류가 발생합니다.

        user.getUserPlaylists().size(); 코드는 UserPlaylist를 명시적으로 로딩하도록 합니다. .size() 메소드를 호출하면 UserPlaylist 컬렉션을 실제로 사용하게 되므로 Hibernate는 UserPlaylist를 로딩합니다. 이렇게 하면 User를 삭제할 때 UserPlaylist도 함께 삭제되므로 데이터 무결성 제약 조건이 유지됩니다.

        이는 JPA와 Hibernate의 작동 방식에 대한 이해를 필요로 하는 복잡한 문제입니다.
        */
        user.getUserPlaylists().size();

        // Remove all relationships
        user.getUserPlaylists().clear();

        // Remove all PlaylistSongs related to the user
        playlistSongRepository.deleteByUser(user);

        // Remove all Playlists related to the user
        playlistRepository.deleteByUser(user);

        // Delete user
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