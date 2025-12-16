package com.ex.center.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ex.center.model.data.*;
import com.ex.center.model.repository.InquiryRepository;
import com.ex.center.model.repository.ReportRepository;
import com.ex.center.model.service.*;
import com.ex.member.model.data.*;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center")
public class CenterController {

    private final InquiryRepository inquiryRepository;
    private final ReportRepository reportRepository;

    private final NoticeService noticeService;
    private final FaqService faqService;

    @GetMapping({ "" })
    public String main(HttpSession session, Model model) {
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        Integer memberRole;
        if (user == null) {
            memberRole = -1;
        } else {
            memberRole = (Integer) user.getMemberRole();
        }
        model.addAttribute("memberRole", memberRole);
        List<NoticeDTO> notices = noticeService.noticeMainList();
        model.addAttribute("notices", notices);

        // 카테고리 기본 선택
        List<FaqDTO> faqs = faqService.faqMainList("");
        model.addAttribute("faqs", faqs);
        model.addAttribute("category", "");

        // 1:1문의, 신고 글 갯수 
        if(user != null){
            int memberNo = user.getMemberNo();
            int inquiryCount = this.inquiryRepository.countByInquiryStatusAndMemberNo(1, memberNo);
            int reportCount = this.reportRepository.countByreportStatusAndMemberNo(1, memberNo);
            model.addAttribute("inquiryCount", inquiryCount);
            model.addAttribute("reportCount", reportCount);
        }
        return "center/main";
    }

    @GetMapping("/search")
    public String searchCenter(@RequestParam("kw") String kw, HttpSession session, Model model) {
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        Integer memberRole;
        if (user == null) {
            memberRole = -1;
        } else {
            memberRole = (Integer) user.getMemberRole();
        }
        model.addAttribute("memberRole", memberRole);
        List<NoticeDTO> noticeResults = noticeService.search(kw);
        List<FaqDTO> faqResults = faqService.search(kw);
        model.addAttribute("kw", kw);
        model.addAttribute("noticeResults", noticeResults);
        model.addAttribute("faqResults", faqResults);
        return "center/search";
    }

    // 메인 FAQ 카테고리 선택시 AJAX : fragment 만 변환
    @PostMapping("/faqMain")
    public String faqListAjax(@RequestParam(defaultValue = "", name = "category") String category,
            Model model) {
        List<FaqDTO> faqs = this.faqService.faqMainList(category);
        model.addAttribute("faqs", faqs);
        model.addAttribute("category", category);
        return "center/main :: faqFragment";
    }

}
