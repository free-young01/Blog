package com.example.Blog.user;

import com.example.Blog.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 이 클래스는 API 요청을 받는 컨트롤러
@RequestMapping("/users") // 이 컨트롤러로 오는 모든 요청은 '/users' 주소로 시작
@RequiredArgsConstructor // final이 붙은 'UserService'를 자동으로 주입(DI)
public class UserController {

    private final UserService userService;

    // JWT 토큰을 담을 HTTP 헤더 이름
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // 'POST /users/signup' 주소로 요청이 오면 이 메서드가 실행
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequestDto requestDto) {
        
        userService.signup(requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
    }

    /** 
     * 로그인 API 엔드포인트
     * @param requestDto 로그인 요청 정보 (이메일, 비밀번호)
     * @param respose HttpServletResponse 객체 (헤더 추가 위해)
     * @return 로그인 성공 시 임시 메시지
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequestDto requestDto, HttpServletResponse response) {
        // 1. Service의 login 메서드 호출 -> JWT 토큰 받기
        String token = userService.login(requestDto);

        // 2. JWT 토큰을 Response Header에 추가
        // 형식: "Bearer [토큰값]"
        response.addHeader(AUTHORIZATION_HEADER, "Bearer " + token);

        // 3. 로그인 성공 응답 (Body는 비워두거나 간단한 메시지)
        return ResponseEntity.ok("로그인 성공");
    }
}