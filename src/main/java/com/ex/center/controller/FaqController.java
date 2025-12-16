package com.ex.center.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ex.center.model.data.FaqDTO;
import com.ex.center.model.data.FaqForm;
import com.ex.center.model.service.FaqService;
import com.ex.member.model.data.MemberDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center/faq")
public class FaqController {

    private final FaqService faqService;

    @GetMapping({ "", "/" })
    public String faqMain(){
        return "redirect:/center/faq/list";
    }

    // Get멥핑 기본 리스트 페이지
    @GetMapping("/list")
    public String faqListView(Model model,
                            @RequestParam(value = "kw", defaultValue = "") String kw,
                            @RequestParam(value = "category", defaultValue = "") String category,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            HttpSession session) {
        // 최초 로드용: 기본값으로 첫 페이지
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        Integer memberRole;
        if (user == null) {
            memberRole = -1;
        } else {
            memberRole = (Integer) user.getMemberRole();
        }
        model.addAttribute("memberRole", memberRole);
        Page<FaqDTO> paging = faqService.faqList(kw, category, 0);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);
        return "center/faqList";
    }

    // AJAX: fragment만 반환
    @PostMapping("/list")
    public String faqListAjax(@RequestParam(defaultValue = "", name = "kw") String kw,
            @RequestParam(defaultValue = "", name = "category") String category,
            @RequestParam(defaultValue = "0", name = "page") int page,
            Model model) {
        Page<FaqDTO> paging = faqService.faqList(kw, category, page);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);
        // 아래에서 정의할 fragment 이름
        return "center/faqList :: listFragment";
    }

    @GetMapping("/insert")
    public String insertFaq(FaqForm form, HttpSession session, Model model) {
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        Integer memberRole;
        if (user == null) {
            memberRole = -1;
        } else {
            memberRole = (Integer) user.getMemberRole();
        }
        model.addAttribute("memberRole", memberRole);
        return "center/faqForm";
    }

    @PostMapping("/insert")
    public String insertFaq(@Valid FaqForm form, BindingResult br) {

        if (br.hasErrors()) {
            return "center/faqForm";
        }
        this.faqService.insertFaq(form);
        return "redirect:/center/faq/list";
    }

    // ajax 세션당 1회 조회수 증가
    @PostMapping("/increase/{faqNo}")
    @ResponseBody
    public String increaseReadCount(@PathVariable("faqNo") int faqNo, HttpSession session) {
        faqService.increaseReadCountOnce(faqNo, session);
        return "ok";
    }
}
