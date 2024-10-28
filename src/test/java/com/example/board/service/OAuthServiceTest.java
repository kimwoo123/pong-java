package com.example.board.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.board.entity.UserInfo;
import com.example.board.repository.UserRepository;
import com.example.board.service.OAuthService;

import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(properties = "spring.config.location=classpath:test-secret.properties")
class OAuthServiceTest {

    // TODO: DELETE
    @Value("${API_URL}")
    private String apiUrl;

    @Mock
    private UserRepository userRepository;

    @MockBean
    private RestTemplate restTemplate;

    @InjectMocks
    private OAuthService oAuthService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${JWT_SECRET}")
    private String jwtSecret;

    private UserInfo userInfo;

    @BeforeEach
    void setUp() {
        userInfo = new UserInfo();
        userInfo.setId(1);
        userInfo.setLogin("testUser");
        userInfo.setEmail("test@example.com");
        userInfo.setImage("profile.png");
        userInfo.setNeedOtp(true);
        userInfo.setIsVerified(false);
    }

//    @Test
//    void testSaveUserInfo() {
//        when(userRepository.existsById(userInfo.getId())).thenReturn(false);
//        when(userRepository.save(any(UserInfo.class))).thenReturn(userInfo);

//        doThrow(new RuntimeException()).doNothing().when(redisTemplate.opsForValue()).set();
//        UserInfo savedUser = oAuthService.saveUserInfo(userInfo);

//        assertThat(savedUser).isEqualTo(userInfo);
//        verify(userRepository, times(1)).save(userInfo);
//        verify(redisTemplate, times(1)).opsForValue().set(String.valueOf(userInfo.getId()), userInfo);
//    }

    @Test
    void testFetchAndSaveUserInfo() throws Exception {
        String accessToken = "access_token";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(createUserInfoMap(), HttpStatus.OK));
        when(userRepository.save(any(UserInfo.class))).thenReturn(userInfo);

        UserInfo fetchedUser = oAuthService.fetchAndSaveUserInfo(accessToken);

        assertThat(fetchedUser).isEqualTo(userInfo);
        verify(userRepository, times(1)).save(any(UserInfo.class));
    }

    @Test
    void testExchangeCodeForToken() throws Exception {
        MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
        mockServer.expect(requestTo(apiUrl + "/oauth/token"))
                .andRespond(withSuccess("{ \"access_token\": \"dummyAccessToken\", \"refresh_token\": \"dummyRefreshToken\" }", MediaType.APPLICATION_JSON));


        System.out.println(apiUrl + "/oauth/token");
        // 테스트 실행 및 결과 검증
        Map<String, String> tokens = oAuthService.exchangeCodeForToken("dummyCode");
        assertEquals("dummyAccessToken", tokens.get("access_token"));
        assertEquals("dummyRefreshToken", tokens.get("refresh_token"));

        mockServer.verify();
    }

    @Test
    void testCreateJwtToken() {
        String jwtToken = oAuthService.createJwtToken("access_token", userInfo.getId());

        assertThat(jwtToken).isNotNull();

        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(jwtToken)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo(userInfo.getId().toString());
        assertThat(claims.get("access_token")).isEqualTo("access_token");
    }

    @Test
    void testExtractJwtFromRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwt", "sample_jwt_token"));

        String jwtToken = oAuthService.extractJwtFromRequest(request);

        assertThat(jwtToken).isEqualTo("sample_jwt_token");
    }

    private Map<String, Object> createUserInfoMap() {
        Map<String, Object> userInfoMap = new HashMap<>();
        userInfoMap.put("id", 1);
        userInfoMap.put("email", "test@example.com");
        userInfoMap.put("login", "testUser");
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("link", "profile.png");
        userInfoMap.put("image", imageMap);
        return userInfoMap;
    }
}