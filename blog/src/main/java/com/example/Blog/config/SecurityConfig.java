package com.example.Blog.config; 

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 이 파일은 스프링의 '설정' 파일
@EnableWebSecurity // 스프링 시큐리티를 활성화
public class SecurityConfig {

    // 1. 비밀번호 암호화 도구(PasswordEncoder)를 Bean으로 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 보안 규칙을 설정하는 FilterChain을 Bean으로 등록
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        // [CSRF 비활성화]
        // JWT(토큰)를 사용할 거라, stateless(상태가 없는) 서버
        // 따라서 세션 기반의 CSRF 보호 기능은 필요 없어서 꺼둠
        http.csrf(csrf -> csrf.disable());

        // [세션 관리 설정]
        // 세션을 사용하지 않는 stateless 서버로 설정
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // [HTTP 요청 권한 설정]
        http.authorizeHttpRequests(authz -> authz
                // " /users/signup " 이 주소로 오는 요청은
                .requestMatchers("/users/signup", "/users/login").permitAll() // 로그인 없이도 모두 허용(permit all)
                
                // 그 외(anyRequest)의 모든 요청은
                .anyRequest().authenticated() // 인증(로그인)이 필요
        );

        // [로그인/로그아웃 설정 비활성화]
        http.formLogin(form -> form.disable()); // 기본 로그인 폼 비활성화
        http.httpBasic(httpBasic -> httpBasic.disable()); // 기본 HTTP Basic 인증 비활성화

        return http.build();
    }
}