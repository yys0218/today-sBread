package com.ex.admin.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ex.admin.model.service.AdminService;
import com.ex.center.model.data.FaqDTO;
import com.ex.center.model.data.FaqForm;
import com.ex.center.model.data.InquiryDTO;
import com.ex.center.model.data.InquiryForm;
import com.ex.center.model.data.NoticeDTO;
import com.ex.center.model.data.NoticeForm;
import com.ex.center.model.data.ReportDTO;
import com.ex.center.model.data.ReportForm;
import com.ex.center.model.service.FaqService;
import com.ex.center.model.service.InquiryService;
import com.ex.center.model.service.ReportService;
import com.ex.center.model.service.NoticeService;
import com.ex.member.model.data.MemberDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/admin/board")
@Controller
@RequiredArgsConstructor
public class AdminBoardController {

    private final NoticeService noticeService;
    private final FaqService faqService;
    private final InquiryService inquiryService;
    private final ReportService reportService;
    private final AdminService adminService;

    /**
     * 관리자페이지 공지사항 관리
     * /admin/board/notice
     */
    @GetMapping("/notice")
    public String adminNotice() {
        return "redirect:/admin/board/notice/list";
    }

    /**
     * 공지목록
     * 요청 url http://localhost:8080/admin/board/notice/list
     */
    @GetMapping("/notice/list")
    public String listNotice(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw,
            @RequestParam(value = "category", defaultValue = "") String category) {
        Page<NoticeDTO> paging = this.noticeService.listNotice(kw, category, page);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);
        model.addAttribute("paging", paging);
        return "admin/board/noticeList";
    }

    /**
     * 공지 내용 보기
     * 요청 url http://localhost:8080/admin/board/notice/detail/글번호
     */
    @GetMapping("/notice/detail/{num}")
    public String detailNotice(Model model, @PathVariable("num") int num) {

        NoticeDTO notice = this.noticeService.getNotice(num);
        model.addAttribute("notice", notice);
        model.addAttribute("num", num);
        return "admin/board/noticeDetail";
    }

    /**
     * 공지 작성
     * 요청 url http://localhost:8080/admin/board/notice/insert
     */
    @GetMapping("/notice/insert")
    public String insertNotice(NoticeForm form) {
        return "admin/board/noticeForm";
    }

    @PostMapping("/notice/insert")
    public String insertNotice(@Valid NoticeForm form, BindingResult br) {

        if (br.hasErrors()) {
            return "admin/board/noticeForm";
        }
        NoticeDTO notice = this.noticeService.insertNotice(form);
        return "redirect:/admin/board/notice/detail/" + notice.getNoticeNo();
    }

    /**
     * 공지 수정하기
     * 요청 url http://localhost:8080/admin/board/notice/update/글번호
     */
    @GetMapping("/notice/update/{noticeNo}")
    public String updateNotice(NoticeForm form, @PathVariable("noticeNo") int noticeNo, Model model) {

        NoticeDTO notice = this.noticeService.getNotice(noticeNo);
        String title = notice.getNoticeTitle();
        if (!title.startsWith("(수정)")) {
            form.setTitle("(수정) " + title);
        } else {
            form.setTitle(title);
        }
        form.setCategory(notice.getNoticeCategory());
        form.setContent(notice.getNoticeContent());
        model.addAttribute("notice", notice);
        return "admin/board/noticeForm";
    }

    @PostMapping("/notice/update/{noticeNo}")
    public String updateNotice(@PathVariable("noticeNo") int noticeNo, @Valid NoticeForm form, BindingResult br) {
        if (br.hasErrors()) {
            return "admin/board/noticeForm";
        }
        this.noticeService.updateNotice(noticeNo, form);
        return "redirect:/admin/board/notice/detail/" + noticeNo;
    }

    /**
     * 공지 숨김처리
     * 요청 url http://localhost:8080/admin/board/notice/delete/글번호
     */
    @GetMapping("/notice/cancel/{noticeNo}")
    public String cancelNotice(@PathVariable("noticeNo") int noticeNo) {
        this.noticeService.cancelNotice(noticeNo);
        return "redirect:/admin/board/notice/list";
    }

    /** 공지 삭제 */
    @GetMapping("/notice/delete/{noticeNo}")
    public String deleteNotice(@PathVariable("noticeNo") int noticeNo) {
        this.noticeService.deleteNotice(noticeNo);
        return "redirect:/admin/board/notice/list";
    }

    /**
     * 관리자페이지 faq 관리하기
     * /admin/board/faq
     */
    @GetMapping("/faq")
    public String adminFaq() {
        return "redirect:/admin/board/faq/list";
    }

    /**
     * faq 리스트
     * 요청 url http://localhost:8080/admin/board/faq/list
     */
    @GetMapping("/faq/list")
    public String faqListView(Model model,
            @RequestParam(name = "kw", defaultValue = "") String kw,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        Page<FaqDTO> paging = faqService.faqList(kw, category, page);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("category", category);
        return "admin/board/faqList";
    }

    /**
     * faq 작성
     * 요청 url http://localhost:8080/admin/board/faq/insert
     */
    @GetMapping("/faq/insert")
    public String insertFaq(FaqForm form) {

        return "admin/board/faqForm";
    }

    @PostMapping("/faq/insert")
    public String insertFaq(@Valid FaqForm form, BindingResult br) {

        if (br.hasErrors()) {
            return "admin/board/faqForm";
        }
        this.faqService.insertFaq(form);
        return "redirect:/admin/board/faq/list";
    }

    /**
     * faq 상세
     * 요청 url http://localhost:8080/admin/board/faq/detail/글번호
     */
    @GetMapping("/faq/detail/{faqNo}")
    public String detailFaq(Model model, @PathVariable("faqNo") int faqNo) {
        FaqDTO faq = this.faqService.getFaq(faqNo);
        model.addAttribute("faq", faq);
        model.addAttribute("faqNo", faqNo);
        return "admin/board/faqDetail";
    }

    /**
     * faq 수정
     * 요청 url http://localhost:8080/admin/board/faq/update/{faqNo}
     */
    @GetMapping("/faq/update/{faqNo}")
    public String updateFaq(FaqForm form, @PathVariable("faqNo") int faqNo, Model model) {

        FaqDTO faq = this.faqService.getFaq(faqNo);
        String title = faq.getFaqTitle();
        if (!title.startsWith("(수정)")) {
            form.setTitle("(수정) " + title);
        } else {
            form.setTitle(title);
        }
        form.setCategory(faq.getFaqCategory());
        form.setContent(faq.getFaqContent());
        model.addAttribute("faq", faq);
        return "admin/board/faqForm";
    }

    @PostMapping("/faq/update/{faqNo}")
    public String updateFaq(@PathVariable("faqNo") int faqNo, @Valid FaqForm form, BindingResult br) {
        if (br.hasErrors()) {
            return "admin/board/faqForm";
        }
        this.faqService.updateFaq(faqNo, form);
        return "redirect:/admin/board/faq/detail/" + faqNo;
    }

    /**
     * faq 숨김처리
     * On/Off 형식
     * 요청 url http://localhost:8080/admin/board/faq/delete/{faqNo}
     */
    @GetMapping("/faq/cancel/{faqNo}")
    public String cancelFaq(@PathVariable("faqNo") int faqNo) {
        this.faqService.cancelFaq(faqNo);
        return "redirect:/admin/board/faq/list";
    }

    /** FAQ 삭제 */
    @GetMapping("/faq/delete/{faqNo}")
    public String deleteFaq(@PathVariable("faqNo") int faqNo) {
        this.faqService.deleteFaq(faqNo);
        return "redirect:/admin/board/faq/list";
    }

    /** 관리자페이지 문의게시판 */
    @GetMapping("/inquiry")
    public String adminInquiry() {
        return "redirect:/admin/board/inquiry/list";
    }

    /**
     * 문의글 목록
     * 요청 url http://localhost:8080/admin/board/inquiry/list
     */
    @GetMapping("/inquiry/list")
    public String adminInquiryList(@RequestParam(defaultValue = "0", name="page") int page,
            @RequestParam(defaultValue = "10", name="size") int size,
            @RequestParam(defaultValue = "inquiryAt", name="sortField") String sortField, // ← 기본은 날짜
            @RequestParam(defaultValue = "desc", name="sortDir") String sortDir,
            @RequestParam(defaultValue = "-1", name="inquiryType") int inquiryType, // ← 필터
            @RequestParam(defaultValue = "-1", name="inquiryStatus") int inquiryStatus, // ← 필터
            Model model) {
        Page<InquiryDTO> paging = inquiryService.adminInquiryList(inquiryType, inquiryStatus, sortField, sortDir, page, size);

        model.addAttribute("paging", paging);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("type", inquiryType);
        model.addAttribute("status", inquiryStatus);
        return "admin/board/inquiryList";
    }

    /**
     * 문의글 상세
     * 요청 url http://localhost:8080/admin/board/inquiry/detail/{inquiryNo}
     */
    @GetMapping("/inquiry/detail/{inquiryNo}") // 조회
    public String inquiryDetail(@PathVariable("inquiryNo") int inquiryNo, Model model) {
        // inquiryNo 로 글 가져와서
        InquiryDTO inquiry = this.inquiryService.getInquiry(inquiryNo);
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("images", inquiry.getInquiryImg());
        return "admin/board/inquiryDetail";
    }

    /**
     * 문의 답변
     * 요청 url http://localhost:8080/admin/board/inquiry/reply/{inquiryNo}
     */
    @GetMapping("/inquiry/reply/{inquiryNo}")
    public String inquiryReply(InquiryForm form, @PathVariable("inquiryNo") int inquiryNo, Model model) {
        InquiryDTO inquiry = inquiryService.getInquiry(inquiryNo);
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
        return "admin/board/inquiryForm";
    }

    @PostMapping("/inquiry/reply/{inquiryNo}")
    public String inquiryReply(@RequestParam("inquiryReply") String inquiryReply,
            @PathVariable("inquiryNo") int inquiryNo) {

        this.inquiryService.inquiryReply(inquiryReply, inquiryNo);
        return "redirect:/admin/board/inquiry/detail/" + inquiryNo;
    }

    /** 관리자페이지 신고게시판 */
    @GetMapping("/report")
    public String adminReport() {
        return "redirect:/admin/board/report/list";
    }

    /**
     * 신고글 목록
     * 요청 url http://localhost:8080/admin/board/report/list
     */
    @GetMapping("/report/list")
    public String adminReportList(@RequestParam(defaultValue = "0", name="page") int page,
            @RequestParam(defaultValue = "10", name="size") int size,
            @RequestParam(defaultValue = "reportAt", name="sortField") String sortField, // ← 기본은 날짜
            @RequestParam(defaultValue = "desc", name="sortDir") String sortDir,
            @RequestParam(defaultValue = "-1", name="reportType") int reportType, // ← 필터
            @RequestParam(defaultValue = "-1", name="reportStatus") int reportStatus, // ← 필터
            Model model) {
        Page<ReportDTO> paging = reportService.adminReportList(reportType, reportStatus, sortField, sortDir, page, size);

        model.addAttribute("paging", paging);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("type", reportType);
        model.addAttribute("status", reportStatus);
        return "admin/board/reportList";
    }

    /**
     * 신고글 상세
     * 요청 url http://localhost:8080/admin/board/report/detail/{reportNo}
     */
    @GetMapping("/report/detail/{reportNo}") // 조회
    public String reportDetail(@PathVariable("reportNo") int reportNo, Model model) {
        // reportNo 로 글 가져와서
        ReportDTO report = this.reportService.getReport(reportNo);
        model.addAttribute("report", report);
        model.addAttribute("images", report.getReportImg());
        return "admin/board/reportDetail";
    }

    /**
     * 신고 답변
     * 요청 url http://localhost:8080/admin/board/report/reply/{reportNo}
     */
    @GetMapping("/report/reply/{reportNo}")
    public String reportReply(ReportForm form, @PathVariable("reportNo") int reportNo, Model model) {
        ReportDTO report = reportService.getReport(reportNo);
        // ReportDTO → ReportForm 으로 변환
        form.setReportType(report.getReportType());
        form.setReportTitle(report.getReportTitle());
        form.setReportContent(report.getReportContent());
        // form.setReportMail(report.getReportMail());

        // 이미지의 경우 MultipartFile이 아니라 DB에 저장된 경로라 바로 넣을 수는 없고,
        // 수정화면에서는 DB에 저장된 이미지 목록을 별도로 model에 넣어줌
        model.addAttribute("images", reportService.getImages(reportNo)); // DB 조회
        model.addAttribute("reportForm", form);
        model.addAttribute("reportNo", reportNo);
        return "admin/board/reportForm";
    }

    @PostMapping("/report/reply/{reportNo}")
    public String reportReply(@RequestParam("reportReply") String reportReply, @PathVariable("reportNo") int reportNo) {

        this.reportService.reportReply(reportReply, reportNo);
        return "redirect:/admin/board/report/detail/" + reportNo;
    }

    /**
     * 문의글 삭제 (관리자)
     * 요청 url http://localhost:8080/admin/board/inquiry/delete/{inquiryNo}
     */
    @GetMapping("/inquiry/delete/{inquiryNo}")
    public String deleteInquiry(@PathVariable("inquiryNo") int inquiryNo) {
        this.inquiryService.deleteInquiry(inquiryNo);
        return "redirect:/admin/board/inquiry/list";
    }

    /**
     * 신고글 삭제 (관리자)
     * 요청 url http://localhost:8080/admin/board/report/delete/{reportNo}
     */
    @GetMapping("/report/delete/{reportNo}")
    public String deleteReport(@PathVariable("reportNo") int reportNo) {
        this.reportService.deleteReport(reportNo);
        return "redirect:/admin/board/report/list";
    }

    /**
     * 게시판 관련 통계
     * 요청 url http://localhost:8080/admin/board/statics
     */
    // @GetMapping("/statics")
}