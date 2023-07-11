package spotifyPlaylist.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spotifyPlaylist.user.dto.UserSettingsDto;
import spotifyPlaylist.user.service.UserService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public UserSettingsDto getUserSettings(@PathVariable Long userId) {
        return userService.getUserSettings(userId);
    }

    @PutMapping("/{userId}")
    public UserSettingsDto updateUserSettings(@PathVariable Long userId, @RequestBody UserSettingsDto userSettingUpdateDto) {
        return userService.updateUserSettings(userId, userSettingUpdateDto);
    }
}