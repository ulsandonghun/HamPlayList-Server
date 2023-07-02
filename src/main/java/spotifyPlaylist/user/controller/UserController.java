package spotifyPlaylist.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spotifyPlaylist.user.dto.FollowerIdDto;
import spotifyPlaylist.user.dto.UpdateDescriptionRequestDto;
import spotifyPlaylist.user.dto.UserSignUpDto;
import spotifyPlaylist.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public String signUp(@RequestBody UserSignUpDto userSignUpDto) throws Exception {
        userService.signUp(userSignUpDto);
        return "회원가입 성공";
    }

    @GetMapping("/jwt-test")
    public String jwtTest() {
        return "jwtTest 요청 성공";
    }

    @PutMapping("/description/{userId}")
    public void updateDescription(@PathVariable Long userId, @RequestBody UpdateDescriptionRequestDto updateDescriptionRequestDto) {
        userService.updateDescription(userId, updateDescriptionRequestDto.getOneLineIntroduction());
    }

    @PostMapping("/follow/{userId}")
    public void followOrUnfollow(@PathVariable Long userId, @RequestBody FollowerIdDto followerIdDto) {
        userService.followOrUnfollow(userId, followerIdDto.getFollowerId());
    }

}
