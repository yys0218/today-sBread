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

import com.ex.center.model.data.NoticeForm;
import com.ex.center.model.data.NoticeDTO;
import com.ex.center.model.service.NoticeService;
import com.ex.member.model.data.MemberDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center/notice")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping({ "", "/" })
    public String noticeMain(){
        return "redirect:/center/notice/list";
    }

    /** 공지list 출력
    요청 url http://localhost:8080/center/notice/list */
    @GetMapping("/list")
    public String listNotice(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "kw", defaultValue = "") String kw,
                            @RequestParam(value = "category", defaultValue = "") String category,
                            HttpSession session) {
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        Integer memberRole;
        if (user == null) {
            memberRole = -1;
        } else {
            memberRole = (Integer) user.getMemberRole();
        }
        model.addAttribute("memberRole", memberRole);
        Page<NoticeDTO> paging = this.noticeService.listNotice(kw, category, page);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);
        model.addAttribute("paging", paging);
        return "center/noticeList";
    }

    // 글 내용 보기
    // 요청 url http://localhost:8080/center/notice/detail/글번호
    @GetMapping("/detail/{num}")
    public String detailNotice(Model model, @PathVariable("num") int num, HttpSession session) {
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        Integer memberRole;
        if (user == null) {
            memberRole = -1;
        } else {
            memberRole = (Integer) user.getMemberRole();
        }
        NoticeDTO notice = this.noticeService.getNotice(num);
        model.addAttribute("notice", notice);
        model.addAttribute("num", num);
        return "center/noticeDetail";
    }

    // 여기서부터는 관리자만
    // 공지사항 작성
    // 요청 url http://localhost:8080/center/notice/insert
    @GetMapping("/insert")
    public String insertNotice(NoticeForm form) {
        return "center/noticeForm";
    }

    @PostMapping("/insert")
    public String insertNotice(@Valid NoticeForm form, BindingResult br) {

        if (br.hasErrors()) {
            return "center/noticeForm";
        }
        NoticeDTO notice = this.noticeService.insertNotice(form);
        return "redirect:/center/notice/detail/" + notice.getNoticeNo();
    }

    // 공지사항 수정하기
    // 요청 url http://localhost:8080/center/notice/update/글번호
    @GetMapping("/update/{num}")
    public String updateNotice(NoticeForm form, @PathVariable("num") int num) {

        NoticeDTO notice = this.noticeService.getNotice(num);
        form.setTitle(notice.getNoticeTitle());
        form.setCategory(notice.getNoticeCategory());
        form.setContent(notice.getNoticeContent());
        return "center/noticeForm";
    }

    @PostMapping("/update/{num}")
    public String updateNotice(@PathVariable("num") int num, @Valid NoticeForm form, BindingResult br) {
        if (br.hasErrors()) {
            return "center/noticeForm";
        }
        this.noticeService.updateNotice(num, form);
        return "redirect:/center/notice/detail/" + num;
    }

    // 공지사항 숨김처리
    // 요청 url http://localhost:8080/center/notice/delete/글번호
    @GetMapping("/delete/{num}")
    public String deleteNotice(@PathVariable("num") int num) {
        this.noticeService.deleteNotice(num);
        return "redirect:/center/notice/list";
    }
}
