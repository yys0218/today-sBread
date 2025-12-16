package com.ex.admin.model.data;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ex.member.model.data.MemberDTO;

/**
 * AdminSecurityDetail
 * 
 * Spring Security에서 인증된 사용자 정보를 저장하는 객체
 * - UserDetails를 구현해야 SecurityContext에서 인식 가능
 * - 로그인 성공 시 세션에 이 객체가 저장됨
 */
public class AdminSecurityDetail implements UserDetails {

    // 실제 우리 시스템에서 사용하는 회원 정보 객체 (MemberDTO)
    private final MemberDTO member;

    // 생성자: 로그인한 사용자(MemberDTO)를 받아 세션에 저장
    public AdminSecurityDetail(MemberDTO member) {
        this.member = member;
    }

    // 세션에서 직접 MemberDTO를 꺼내 쓰고 싶을 때 사용
    public MemberDTO getMember() {
        return member;
    }

    // ====================== UserDetails 필수 구현 메서드 ======================

    // 사용자 아이디 리턴
    // - Spring Security에서 username으로 인식
    // - 여기서는 memberId를 username으로 사용

    @Override
    public String getUsername() {
        return member.getMemberId();
    }

    // 사용자 비밀번호 리턴
    // - Spring Security에서 인증 시 사용하는 비밀번호
    // - 반드시 암호화(BCrypt 등)된 값이어야 함

    @Override
    public String getPassword() {
        return member.getMemberPw();
    }

    // 사용자 권한(Role) 리턴
    // - ROLE_USER, ROLE_ADMIN 등 권한 목록을 반환해야 함
    // - 현재는 null 반환 → 권한 체크가 필요한 경우 반드시 구현 필요

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    // 계정 만료 여부
    // - true → 만료되지 않음 (항상 사용 가능)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부
    // - true → 잠기지 않음 (항상 로그인 가능)

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부
    // - true → 만료되지 않음 (항상 로그인 가능)

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부
    // - true → 활성화된 계정 (항상 로그인 가능)

    @Override
    public boolean isEnabled() {
        return true;
    }

}
