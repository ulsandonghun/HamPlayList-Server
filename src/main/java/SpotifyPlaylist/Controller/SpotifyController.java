package SpotifyPlaylist.Controller;

import SpotifyPlaylist.DTO.SearchResponseDto;
import SpotifyPlaylist.SpotifyConfig;
import java.io.IOException;
import java.util.List;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import SpotifyPlaylist.Service.SpotifyService;
@RestController
public class SpotifyController {
    SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setAccessToken(SpotifyConfig.accessToken())
            //accessToken()은 SpotifyConfig 에서 발급함
            .build();

    @Autowired
    SpotifyService spotifyService =new SpotifyService();

    @GetMapping("/search/{trackname}")
    public List<SearchResponseDto> searchTracksByTrackname(@PathVariable String trackname) throws IOException, ParseException, SpotifyWebApiException {


        return spotifyService.SearchByTrackname(trackname);
    }

    @GetMapping("/search/{artist}/{trackname}")
    public List<SearchResponseDto> searchTracksByTracknameAndArtist(@PathVariable String trackname,@PathVariable String artist) throws IOException, ParseException, SpotifyWebApiException {


        return spotifyService.SearchByTracknameAndArtist(trackname,artist);
    }

}
