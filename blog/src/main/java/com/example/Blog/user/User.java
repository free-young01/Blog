package com.example.Blog.user;

import jakarta.persistence.*; // JPA 어노테이션
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter // Lombok : 이 클래스 모든 필드에 Getter 자동 생성
@Setter // Lombok : Setter 자동 생성
@NoArgsConstructor // Lombok : 기본 생성자 자동 생성
@Entity // 데이터베이스 테이블과 매핑 (JPA)
@Table(name = "users") // 데이터베이스의 "users" 테이블과 연결

public class User {

    @Id // 이 필드가 기본 키
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 알아서 ID 1씩 증가
    private Long id;

    @Column(nullable = false, unique = true) // null 안됨, 중복 안됨
    private String email;

    @Column(nullable = false) // null 안됨
    private String password;

    @Column(nullable = false, unique = true) // null 안됨, 중복 안됨
    private String nickname;

    @Column // 특별한 제약 없음
    private String organizattion;

    @Column(columnDefinition = "TEXT") // text 타입
    private String bio;

    @Column(name = "is_public")
    boolean isPublic = true;

    @CreationTimestamp // 데이터가 생성될 때 현재 시간 자동 저장
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
