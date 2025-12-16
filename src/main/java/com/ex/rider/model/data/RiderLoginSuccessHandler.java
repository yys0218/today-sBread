package com.ex.rider.model.data;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.ex.member.model.data.MemberDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RiderLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        RiderSecurityDetail detail = (RiderSecurityDetail) authentication.getPrincipal();
        MemberDTO member = detail.getMember();

        if (member.getIsTemporary() == 1) {
            // 임시 비밀번호 → 비밀번호 변경 페이지로
            response.sendRedirect("/rider/change-password");
        } else {
            // 정상 계정 → 라이더 메인 페이지로
            response.sendRedirect("/rider");
        }

    }

}
