package com.ex.admin.controller;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ex.admin.model.service.AdminService;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("")
    public String admin(HttpSession session, Model model) {
        session.invalidate();
        return "/admin/login"; // 메인으로 이동하기 전 알림 띄우기
    }

    @GetMapping("/login")
    public String adminLogin() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam("username") String username, @RequestParam("password") String password,
            HttpSession session, RedirectAttributes ra, Model model) {

        Optional<MemberDTO> _member = this.memberRepository.findByMemberId(username);
        if (_member.isEmpty()) {
            model.addAttribute("title", "로그인 실패");
            model.addAttribute("msg", "아이디가 잘못되었습니다.");
            model.addAttribute("icon", "error");
            model.addAttribute("loc", "/admin");
            return "common/msg"; // 메인으로 이동하기 전 알림 띄우기
        }
        MemberDTO member = _member.get();
        String inputPw = password.trim();
        boolean result = passwordEncoder.matches(inputPw, member.getMemberPw());

        if (!(result && member.getMemberRole() == 2)) {
            model.addAttribute("title", "경고");
            model.addAttribute("msg", "관리자가 아니면 접근할 수 없습니다.");
            model.addAttribute("icon", "warning");
            model.addAttribute("loc", "/");
            return "common/msg"; // 메인으로 이동하기 전 알림 띄우기
        }
        session.setAttribute("user", member);
        model.addAttribute("title", "확인되었습니다.");
        model.addAttribute("msg", "어서오세요, 관리자님.");
        model.addAttribute("icon", "success");
        model.addAttribute("loc", "/admin/main");
        return "common/msg"; // 메인으로 이동하기 전 알림 띄우기
    }

    @GetMapping("/main")
    public String adminMain(HttpSession session, Model model) {
        if (session.getAttribute("user") == null) {
            model.addAttribute("title", "경고");
            model.addAttribute("msg", "관리자가 아니면 접근할 수 없습니다.");
            model.addAttribute("icon", "warning");
            model.addAttribute("loc", "/admin");
            return "common/msg";
        } else {
            MemberDTO member = (MemberDTO) session.getAttribute("user");
            if (member.getMemberRole() != 2) {
                model.addAttribute("title", "경고");
                model.addAttribute("msg", "관리자가 아니면 접근할 수 없습니다.");
                model.addAttribute("icon", "warning");
                model.addAttribute("loc", "/admin");
                return "common/msg"; // 메인으로 이동하기 전 알림 띄우기
            }
            return "redirect:/admin/main/dashBoard";
        }
    }

}
