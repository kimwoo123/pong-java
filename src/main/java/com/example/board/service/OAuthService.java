package com.example.board.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import io.jsonwebtoken.*;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.example.board.repository.UserRepository;
import com.example.board.entity.UserInfo;

@PropertySource("/secret.properties")
@Service
public class OAuthService {

    @Value("${API_URL}")
    private String apiUrl;

    @Value("${INTRA_UID}")
    private String intraUid;

    @Value("${INTRA_SECRET_KEY}")
    private String intraSecretKey;

    @Value("${REDIRECT_URI}")
    private String redirectUri;

    @Value("${AUTH_PAGE}")
    private String authPage;

    @Value("${STATE}")
    private String state;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${FRONT_BASE_URL}")
    private String frontBaseUrl;

    private final int JWT_EXPIRED = 7000;

    @Autowired
    private UserRepository userRepository;

    public UserInfo saveUserInfo(UserInfo userInfo) {
//        System.out.println("Id: " + userInfo.getId() + "Login: " + userInfo.getLogin()
//                + " Email: "+ userInfo.getEmail()
//                + "Image: " + userInfo.getImage()
//                + "Country: " + userInfo.getCountry());
        return userRepository.save(userInfo);
    }

    public void sendToApi(HttpServletResponse response) throws IOException {
        response.sendRedirect(authPage);
    }

    public UserInfo fetchAndSaveUserInfo(String accessToken) throws IOException {
        UserInfo userInfo = getUserInfo(accessToken);

        return saveUserInfo(userInfo);
    }

    public Map<String, String> exchangeCodeForToken(String code) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String url = apiUrl + "/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", intraUid);
        body.add("client_secret", intraSecretKey);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, String> tokens = new HashMap<>();
            tokens.put("access_token", (String) response.getBody().get("access_token"));
            tokens.put("refresh_token", (String) response.getBody().get("refresh_token"));
            return tokens;
        }

        return null;
    }

    public UserInfo getUserInfo(String accessToken) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String url = apiUrl + "/v2/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        Map<String, Object> image = (Map<String, Object>) response.getBody().get("image");
        String imageLink = (String) image.get("link");
        System.out.println(response.getBody());

        if (response.getStatusCode() == HttpStatus.OK) {
            UserInfo userInfo = new UserInfo();
            userInfo.setId((Integer) response.getBody().get("id"));
            userInfo.setEmail((String) response.getBody().get("email"));
            userInfo.setLogin((String) response.getBody().get("login"));
            userInfo.setImage(imageLink);
            userInfo.setIsVerified(false);
            userInfo.setNeedOtp(false);
            return userInfo;
        }

        throw new RuntimeException("Failed to retrieve user info");
    }

    public String createJwtToken(String accessToken, Integer userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRED);  // Set expiry time

        return Jwts.builder()
                .setSubject(Integer.toString(userId))
                .claim("access_token", accessToken)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getRedirectUrl(boolean needOtp, boolean isVerified) {
        if (needOtp) {
            if (!isVerified) {
                return frontBaseUrl + "/QRcode";
            } else {
                return frontBaseUrl + "/OTP";
            }
        } else {
            return frontBaseUrl + "/main";
        }
    }

    public void clearUserCache(Long userId) {
        // Implement cache clearing logic here
    }

    public String extractJwtFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public Long decodeJwt(String jwtToken) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(jwtToken)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }
}
