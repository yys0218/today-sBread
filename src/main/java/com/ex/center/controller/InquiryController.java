package com.ex.center.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import com.ex.center.model.data.InquiryDTO;
import com.ex.center.model.data.InquiryForm;
import com.ex.center.model.repository.InquiryRepository;
import com.ex.center.model.service.InquiryService;
import com.ex.member.model.data.MemberDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center/inquiry")
public class InquiryController {

    private final InquiryService inquiryService;
    private final InquiryRepository inquiryRepository;

    @GetMapping({ "", "/" })
    public String inquiryMain() {
        return "redirect:/center/inquiry/list";
    }

    @GetMapping("/list")
    public String inquiryList(HttpSession session,
            @RequestParam(defaultValue = "-1", name = "status") Integer status,
            @RequestParam(defaultValue = "0", name = "page") int page,
            Model model) {
        if (session.getAttribute("user")==null) {
            model.addAttribute("title", "로그인이 필요합니다.");
            model.addAttribute("msg", "");
            model.addAttribute("icon", "warning");
            model.addAttribute("loc", "/member/login");
            return "common/msg";
        }
        MemberDTO user = (MemberDTO)session.getAttribute("user");
        model.addAttribute("memberRole", user.getMemberRole());
        int memberNo = user.getMemberNo();
        Page<InquiryDTO> paging = inquiryService.inquiryList(memberNo, status, page);
        model.addAttribute("paging", paging);
        model.addAttribute("status", status);
        return "center/inquiryList";
    }

    @GetMapping("/detail/{inquiryNo}") // 조회
    public String inquiryDetail(@SessionAttribute("user") MemberDTO user,
            @PathVariable("inquiryNo") int inquiryNo,
            Model model) {
        model.addAttribute("memberRole", user.getMemberRole());
        // inquiryNo 로 글 가져와서
        InquiryDTO inquiry = this.inquiryService.getInquiry(inquiryNo);
        // 본인 확인
        if (user.getMemberNo() != inquiry.getMemberNo()) {
            return "redirect:/center/inquiry";
        }
        String memberNick = user.getMemberNick();
        if (inquiry.getInquiryStatus() == 1) {
            inquiry.setInquiryStatus(2);
        }
        // 맞으면 model에 넣어서 리턴
        this.inquiryRepository.save(inquiry);
        model.addAttribute("memberNick", memberNick);
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("images", inquiry.getInquiryImg());
        return "center/inquiryDetail";
    }

    @GetMapping("/insert") // 작성
    public String inquiryInsert(InquiryForm form, @SessionAttribute("user") MemberDTO user, Model model) {
        model.addAttribute("memberRole", user.getMemberRole());
        return "center/inquiryForm";
    }

    @PostMapping("/insert")
    public String inquiryInser(@SessionAttribute("user") MemberDTO user,
            @Valid InquiryForm form,
            BindingResult br) {
        if (br.hasErrors()) {
            return "center/inquiryForm";
        }
        if (form.getImages() != null) {
            for (MultipartFile f : form.getImages()) {
            }
        }

        int memberNo = user.getMemberNo();
        InquiryDTO inquiry = this.inquiryService.insertInquiry(memberNo, form);
        return "redirect:/center/inquiry/list";
    }

    @GetMapping("/update/{inquiryNo}")
    public String inquiryUpdateForm(InquiryForm form, @SessionAttribute("user") MemberDTO user,
            @PathVariable("inquiryNo") int inquiryNo,
            Model model) {
        model.addAttribute("memberRole", user.getMemberRole());

        // 1. 해당 글 불러오기
        InquiryDTO inquiry = inquiryService.getInquiry(inquiryNo);

        // 2. 본인 글인지 체크 (아닐 경우 접근 차단)
        if (inquiry.getMemberNo() != user.getMemberNo()) {
            return "redirect:/center/inquiry"; // 목록으로 돌려보내기
        }

        // InquiryDTO → InquiryForm 으로 변환
        form.setInquiryType(inquiry.getInquiryType());
        form.setInquiryTitle(inquiry.getInquiryTitle());
        form.setInquiryContent(inquiry.getInquiryContent());
        // form.setInquiryMail(inquiry.getInquiryMail());

        // 이미지의 경우 MultipartFile이 아니라 DB에 저장된 경로라 바로 넣을 수는 없고,
        // 수정화면에서는 DB에 저장된 이미지 목록을 별도로 model에 넣어줌
        model.addAttribute("images", inquiryService.getImages(inquiryNo)); // DB 조회
        model.addAttribute("inquiryForm", form);
        model.addAttribute("inquiryNo", inquiryNo);

        return "center/inquiryForm";
    }

    @PostMapping("/update/{inquiryNo}")
    public String inquiryUpdate(@SessionAttribute("user") MemberDTO user,
            @PathVariable("inquiryNo") int inquiryNo,
            @Valid InquiryForm form,
            BindingResult br,
            @RequestParam(value = "deleteImgIds", required = false) List<Integer> deleteImgIds,
            Model model) {
        model.addAttribute("memberRole", user.getMemberRole());
        if (br.hasErrors()) {
            model.addAttribute("inquiryNo", inquiryNo);
            model.addAttribute("images", inquiryService.getImages(inquiryNo));
            return "center/inquiryForm";
        }

        // 본인 글 확인
        InquiryDTO inquiry = inquiryService.getInquiry(inquiryNo);
        if (inquiry.getMemberNo() != user.getMemberNo()) {
            return "redirect:/center/inquiry";
        }

        // 서비스 호출 (수정 처리)
        inquiryService.updateInquiry(inquiryNo, form, deleteImgIds);
        return "redirect:/center/inquiry/list";
    }

    @PostMapping("/delete/{inquiryNo}") // 철회
    public String deleteInquiry(@SessionAttribute("user") MemberDTO user, @PathVariable("inquiryNo") int inquiryNo,
            Model model) {

        InquiryDTO inquiry = this.inquiryService.getInquiry(inquiryNo);
        int memberNo = user.getMemberNo();
        if (inquiry.getMemberNo() != memberNo) {
            model.addAttribute("memberRole", user.getMemberRole());
            return "redirect:/center/inquiry";
        }
        this.inquiryService.deleteInquiry(inquiryNo);
        model.addAttribute("memberRole", user.getMemberRole());
        return "redirect:/center/inquiry";
    }

}
