package com.example.Blog.user;

import com.example.Blog.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 

import java.util.Optional;

@Service // 이 클래스는 스프링의 '서비스' 계층
@RequiredArgsConstructor // final이 붙은 필드들을 자동으로 주입(DI)하는 생성자
public class UserService {

    // 'final' 키워드는 이 객체가 한 번 주입되면 절대 바뀌지 않음
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 비즈니스 로직
     * @param requestDto 회원가입 요청 정보
     */
    @Transactional // "이 메서드 안의 모든 DB 작업은 하나의 '거래'로 묶입니다."
    public void signup(UserSignupRequestDto requestDto) {
        
        // 1. 이메일 중복 검사
        String email = requestDto.getEmail();
        Optional<User> checkEmail = userRepository.findByEmail(email);
        if (checkEmail.isPresent()) {
            // .isPresent() : 'Optional' 상자 안에 무언가(User)가 들어있다면
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 비밀번호 암호화
        String rawPassword = requestDto.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 3. DTO를 -> Entity로 변환 (DB에 저장할 'User' 객체 생성)
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword); // 암호화된 비번을 저장
        user.setNickname(requestDto.getNickname());
        // isPublic, createdAt 등은 Entity의 기본값/설정(@CreationTimestamp)으로 자동 처리됨

        // 4. DB에 저장
        userRepository.save(user);
    }

    /**
     * 로그인 비즈니스 로직
     * @param requestDto 로그인 요청 정보 (이메일, 비밀번호)
     */
    @Transactional (readOnly = true) // 읽기 전용 트랜잭션
    public String login(UserLoginRequestDto requestDto){
        // 1. 이메일로 사용자 조회
        String email = requestDto.getEmail();
        User user = userRepository.findByEmail(email)
        .orElseThrow(()->new IllegalArgumentException("등록되지 않은 이메일입니다.")); // Optional 처리

        // 2. 비밀번호 일치 여부 확인
        String rawPassword = requestDto.getPassword();
        String encodedPassword = user.getPassword(); // DB에 저장된 암호화된 비밀번호

        // passwordEncoder.matches(평문 비밀번호, 암호화된 비밀번호) -> 일치하면 ture, 아니면 false
        if (!passwordEncoder.matches(rawPassword, encodedPassword)){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 로그인 성공 -> JWT 토큰 생성 및 반환
        return jwtUtil.createToken(user.getEmail()); // 사용자 이메일로 토큰 생성
    }
}