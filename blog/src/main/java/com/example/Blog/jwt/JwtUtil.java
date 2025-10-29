package com.example.Blog.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component // 스프링 빈으로 등록해서 다른 곳에서 주입(DI)받아 사용할 수 있게 함
public class JwtUtil {

    // JWT 생성 및 검증에 사용할 비밀키 
    // application.properties 또는 application.yml 파일에서 설정값을 가져옴
    @Value("${jwt.secret.key}")
    private String secretKeyString;

    // 비밀키 객체
    private SecretKey secretKey;

    // 토큰 만료 시간 (밀리초 단위, 여기서는 1시간)
    private final long expirationTime = 60 * 60 * 1000L;

    // 객체 생성 후 비밀키 초기화 수행
    @PostConstruct
    public void init() {
        // Base64로 인코딩된 비밀키 문자열을 디코딩하여 SecretKey 객체 생성
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 사용자 이메일을 기반으로 JWT 토큰 생성
     * @param email 사용자 이메일
     * @return 생성된 JWT 토큰 문자열
     */
    public String createToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(email) // 토큰의 주체(사용자 식별 정보, 여기서는 이메일 사용)
                .setIssuedAt(now) // 토큰 발급 시간
                .setExpiration(expiryDate) // 토큰 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256) // 비밀키와 HS256 알고리즘으로 서명
                .compact(); // 압축하여 최종 토큰 문자열 생성
    }

    // --- (아래는 나중에 토큰 검증 시 사용할 메서드들 - 지금은 일단 비워둠) ---

    /**
     * HTTP 요청 헤더에서 JWT 토큰 추출
     * @param bearerToken "Bearer [토큰값]" 형태의 문자열
     * @return 실제 토큰값 또는 null
     */
    public String resolveToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 문자열 반환
        }
        return null;
    }

    /**
     * JWT 토큰의 유효성 검증 (만료 여부, 서명 일치 여부 등)
     * @param token 검증할 토큰 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            // 비밀키를 사용하여 토큰 파싱 시도 (실패 시 예외 발생)
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 토큰이 유효하지 않은 경우 (만료, 변조 등)
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰에서 사용자 이메일(Subject) 추출
     * @param token 토큰 문자열
     * @return 사용자 이메일
     */
    public String getUserEmailFromToken(String token) {
        // 비밀키를 사용하여 토큰의 Claim(정보 조각) 파싱
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }
}