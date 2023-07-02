package spotifyPlaylist.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spotifyPlaylist.user.dto.UserSettingResponseDto;
import spotifyPlaylist.user.service.UserService;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public UserSettingResponseDto getUserSettings(@PathVariable Long userId) {
        return userService.getUserSettings(userId);
    }
}
