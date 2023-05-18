package SpotifyPlaylist.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import org.springframework.web.util.UriUtils;

@Controller
@RequestMapping("/spotify")
public class SpotifyAuthController {

    @Value("${spotify.registration.client-id}")
    private String clientId;

    @Value("${spotify.registration.client-secret}")
    private String clientSecret;

    @Value("${spotify.registration.redirect-uri}")
    private String redirectUri;

    private final Random random = new Random();
    private final String stateKey = "spotify_auth_state";

    @GetMapping("/login")
    public RedirectView login(HttpServletRequest request, HttpServletResponse response) {
        String state = generateRandomString(16);
        response.addCookie(new Cookie(stateKey, state));

        String scope = "user-read-private user-read-email";
        String authorizeUrl = "https://accounts.spotify.com/authorize";

        URI uri = UriComponentsBuilder.fromUriString(authorizeUrl)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", scope)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build().toUri();

        return new RedirectView(uri.toString());
    }

    @GetMapping("/callback")
    @ResponseBody
    public RedirectView callback(@RequestParam(value = "code", required = false) String code,
                                 @RequestParam(value = "state", required = false) String state,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException {

        String storedState = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (stateKey.equals(cookie.getName())) {
                    storedState = cookie.getValue();
                    break;
                }
            }
        }

        if (state == null || !state.equals(storedState)) {
            response.sendRedirect("/#" + encodeQueryParam("error", "state_mismatch"));
        } else {
            response.addCookie(new Cookie(stateKey, null));

            String tokenUrl = "https://accounts.spotify.com/api/token";

            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            URI uri = UriComponentsBuilder.fromUriString(tokenUrl).build().toUri();

            String requestBody = "code=" + code +
                    "&redirect_uri=" + encodeQueryParam(redirectUri) +
                    "&grant_type=authorization_code";

            HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, createHeaders(encodedCredentials));

            ResponseEntity<TokenResponse> tokenResponseEntity = new RestTemplate().postForEntity(uri, httpEntity, TokenResponse.class);
            if (tokenResponseEntity.getStatusCode() == HttpStatus.OK) {
                TokenResponse tokenResponse = tokenResponseEntity.getBody();
                String accessToken = tokenResponse.getAccess_token();
                String refreshToken = tokenResponse.getRefresh_token();

                String userInfoUrl = "https://api.spotify.com/v1/me";
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                HttpEntity<String> userInfoEntity = new HttpEntity<>(headers);
                ResponseEntity<UserInfoResponse> userInfoResponseEntity = new RestTemplate().exchange(userInfoUrl, HttpMethod.GET, userInfoEntity, UserInfoResponse.class);
                if(userInfoResponseEntity.getStatusCode() == HttpStatus.OK) {
                    UserInfoResponse userInfoResponse = userInfoResponseEntity.getBody();
// Process the user information as needed
                    response.sendRedirect("/#" +
                            encodeQueryParam("access_token", accessToken) +
                            "&" +
                            encodeQueryParam("refresh_token", refreshToken));
                } else {
                    response.sendRedirect("/#" + encodeQueryParam("error", "invalid_token"));
                }
            } else {
                response.sendRedirect("/#" + encodeQueryParam("error", "invalid_token"));
            }
        }

        return null;
    }

    @GetMapping("/refresh_token")
    @ResponseBody
    public ResponseEntity<TokenResponse> refreshToken(@RequestParam("refresh_token") String refreshToken) {
        String tokenUrl = "https://accounts.spotify.com/api/token";
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        URI uri = UriComponentsBuilder.fromUriString(tokenUrl).build().toUri();

        String requestBody = "refresh_token=" + encodeQueryParam(refreshToken) +
                "&grant_type=refresh_token";

        HttpEntity<String> httpEntity = new HttpEntity<>(requestBody, createHeaders(encodedCredentials));

        return new RestTemplate().postForEntity(uri, httpEntity, TokenResponse.class);
    }

    private HttpHeaders createHeaders(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + authorization);
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    private String generateRandomString(int length) {
        String possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(possible.length());
            builder.append(possible.charAt(randomIndex));
        }
        return builder.toString();
    }

    private String encodeQueryParam(String param) {
        return UriUtils.encodeQueryParam(param, StandardCharsets.UTF_8);
    }

    private String encodeQueryParam(String name, String value) {
        return UriUtils.encodeQueryParam(name, StandardCharsets.UTF_8) +
                "=" +
                UriUtils.encodeQueryParam(value, StandardCharsets.UTF_8);
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<String>> getMe() {
        if (accessToken.get() == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized"));
        }

        String meUrl = "https://api.spotify.com/v1/me";
        return webClient.get()
                .uri(meUrl)
                .header("Authorization", "Bearer " + accessToken.get())
                .retrieve()
                .toEntity(String.class);
    }

}
class TokenResponse {
    private String access_token;
    private String token_type;
    private String scope;
    private int expires_in;
    private String refresh_token;
    // Getters and Setters

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}

class UserInfoResponse {
    private String id;
    private String display_name;
    private String email;
}