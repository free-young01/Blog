package com.example.Blog.jwt;

import com.example.Blog.user.User; // User 엔티티 import
import com.example.Blog.user.UserRepository; // UserRepository import
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter; // 상속받을 필터 클래스

import java.io.IOException;
import java.util.ArrayList; // 임시 권한 목록 위해

// OncePerRequestFilter: 모든 서블릿 요청에 대해 단 한 번만 실행되는 필터를 만들기 위한 편리한 추상 클래스
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository; // 사용자 정보 조회를 위해 추가 (UserDetailsServiceImpl 대신)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 "Bearer " 토큰 가져오기
        String bearerToken = request.getHeader("Authorization"); // UserController의 AUTHORIZATION_HEADER와 동일
        String token = jwtUtil.resolveToken(bearerToken);

        // 2. 토큰이 유효한지 검증
        if (token != null && jwtUtil.validateToken(token)) {
            // 3. 토큰에서 사용자 정보(이메일) 가져오기
            String email = jwtUtil.getUserEmailFromToken(token);

            // 4. 이메일을 사용하여 데이터베이스에서 사용자 정보 조회 (실제 사용자 확인)
            // (Optional 대신 간단히 처리. 실제로는 UserDetailsServiceImpl을 사용하는 것이 더 표준적)
            User user = userRepository.findByEmail(email)
                    .orElse(null); // 사용자가 없으면 null 반환 (실제로는 예외 처리 필요)

            if (user != null) {
                // 5. UserDetails 객체 생성 (여기서는 User 엔티티 정보를 직접 사용)
                // 임시 UserDetails 객체 생성 (사용자 이름, 비밀번호 null, 권한 목록 비어 있음)
                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("") // 비밀번호는 사용하지 않으므로 비워둠
                        .authorities(new ArrayList<>()) // 권한 목록 (추후 역할 기반으로 설정)
                        .build();

                // 6. Spring Security 인증 토큰 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // 7. SecurityContextHolder에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}