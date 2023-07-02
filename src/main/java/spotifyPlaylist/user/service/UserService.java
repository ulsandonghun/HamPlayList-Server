package spotifyPlaylist.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import spotifyPlaylist.user.domain.Role;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.dto.UserSignUpDto;
import spotifyPlaylist.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

}