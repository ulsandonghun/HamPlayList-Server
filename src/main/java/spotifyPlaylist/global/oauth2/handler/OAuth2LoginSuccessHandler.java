package spotifyPlaylist.global.oauth2.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import spotifyPlaylist.global.jwt.service.JwtService;
import spotifyPlaylist.global.oauth2.CustomOAuth2User;
import spotifyPlaylist.user.domain.User;
import spotifyPlaylist.user.repository.UserRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
@Slf4j
@Component
@RequiredArgsConstructor
//@Transactional
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;  // Jackson's ObjectMapper to convert Java object to JSON

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Login 성공!");
        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getEmail();

            String accessToken;

            // UserRepository를 사용하여 DB에서 사용자 정보 조회
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) { // 사용자 정보가 존재하면 (회원가입이 되어 있으면)

                accessToken = loginSuccess(response, oAuth2User); // 로그인에 성공한 경우 access, refresh 토큰 생성


//                response.sendRedirect("http://localhost:3000/oauth2/redirect/?Token=" + accessToken); // 메인 페이지로 리다이렉트
                response.sendRedirect("http://localhost:8080/oauth2/redirect/?Token=" + accessToken); // 메인 페이지로 리다이렉트

            } else { // 사용자 정보가 존재하지 않으면 (회원가입이 안 되어 있으면)
                 accessToken = jwtService.createAccessToken(email);
                response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
                response.sendRedirect("/signup"); // 회원가입 페이지로 리다이렉트
                jwtService.sendAccessAndRefreshToken(response, accessToken, null);
            }

        } catch (Exception e) {
            throw e;
        }

    }

    // TODO : 소셜 로그인 시에도 무조건 토큰 생성하지 말고 JWT 인증 필터처럼 RefreshToken 유/무에 따라 다르게 처리해보기

    private String loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken();
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
        return accessToken;
    }
}
