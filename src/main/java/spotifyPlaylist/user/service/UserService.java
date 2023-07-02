package spotifyPlaylist.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spotifyPlaylist.user.domain.Follow;
import spotifyPlaylist.user.domain.Role;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.dto.UserDto;
import spotifyPlaylist.user.dto.UserSignUpDto;
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
    public void updateDescription(Long userId, String oneLineIntroduction) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            user.get().setOneLineIntroduction(oneLineIntroduction);
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
                    userDto.setOneLineIntroduction(follow.getFollower().getOneLineIntroduction());
                    return userDto;
                })
                .collect(Collectors.toList());
    }

}