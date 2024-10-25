package com.example.board.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.board.entity.UserInfo;
import com.example.board.dto.ArticleForm;
import com.example.board.service.OAuthService;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class OAuthController {

    private final OAuthService oAuthService;

    public OAuthController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @GetMapping("/login")
    public void redirectApi(HttpServletResponse response) {
        try {
            oAuthService.sendToApi(response);
        } catch (Exception e) {
            ;
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<?> getOAuthToken(@RequestParam String code, HttpServletResponse response) {
        try {
            Map<String, String> tokens = oAuthService.exchangeCodeForToken(code);
            if (tokens == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to obtain token");
            }

            UserInfo userInfo = oAuthService.fetchAndSaveUserInfo(tokens.get("access_token"));
            String jwtToken = oAuthService.createJwtToken(tokens.get("access_token"), userInfo.getId());
            String redirectUrl = oAuthService.getRedirectUrl(userInfo.getNeedOtp(), userInfo.getIsVerified());

            Cookie jwtCookie = new Cookie("jwt", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String jwtToken = oAuthService.extractJwtFromRequest(request);
        Long userId = oAuthService.decodeJwt(jwtToken);

        oAuthService.clearUserCache(userId);

        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");

        return ResponseEntity.ok("Logout success");
    }

    @GetMapping("/article")
    public String newArticleForm() {
        return "articles/form";
    }

    @PostMapping("/article")
    public String createArticle(ArticleForm form) {
        System.out.println(form.toString());
        return "";
    }
}
