package com.ex.center.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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

import com.ex.center.model.data.ReportDTO;
import com.ex.center.model.data.ReportForm;
import com.ex.center.model.repository.ReportRepository;
import com.ex.center.model.service.ReportService;
import com.ex.member.model.data.MemberDTO;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center/report")
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;
    // private final 

    @GetMapping({ "", "/" })
    public String reportMain() {
        return "redirect:/center/report/list";
    }

    /** 신고글 조회
    * url = localhost:8080/center/report/list
    */
    @GetMapping("/list")
    public String reportList(HttpSession session,
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
        Page<ReportDTO> paging = reportService.reportList(memberNo, status, page);
        model.addAttribute("paging", paging);
        model.addAttribute("status", status);
        return "center/reportList";
    }

    /** 신고글 상세조회
    * url = localhost:8080/center/report/detail/{reportNo}
    */
    @GetMapping("/detail/{reportNo}") 
    public String reportDetail(@SessionAttribute("user") MemberDTO user,
            @PathVariable("reportNo") int reportNo,
            Model model) {
        model.addAttribute("memberRole", user.getMemberRole());
        // reportNo 로 글 가져와서
        ReportDTO report = this.reportService.getReport(reportNo);
        // 본인 확인
        if (user.getMemberNo() != report.getMemberNo()) {
            return "redirect:/center/report";
        }
        // 맞으면 model에 넣어서 리턴
        String memberNick = user.getMemberNick();
        if(report.getReportStatus()==1){report.setReportStatus(2);}
        this.reportRepository.save(report);
        model.addAttribute("memberNick", memberNick);
        model.addAttribute("report", report);
        model.addAttribute("images", report.getReportImg());
        return "center/reportDetail";
    }

    /** 신고글 작성
    * url = localhost:8080/center/report/insert
    */
    @GetMapping("/insert") 
    public String reportInsert(ReportForm form, HttpSession session, Model model) {
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        Integer memberRole;
        if (user == null) {
            memberRole = -1;
        } else {
            memberRole = user.getMemberRole();
        }
        model.addAttribute("memberRole", memberRole);
        return "center/reportForm";
    }

    @PostMapping("/insert")
    public String reportInser(@SessionAttribute("user") MemberDTO user,
            @Valid ReportForm form,
            BindingResult br) {
        if (br.hasErrors()) {
            return "center/reportForm";
        }
        if (form.getImages() != null) {
            for (MultipartFile f : form.getImages()) {
            }
        }

        
        int memberNo = user.getMemberNo();
        ReportDTO report = this.reportService.insertReport(memberNo, form);
        return "redirect:/center/report/list";
    }

    /** 신고글 수정
    * url = localhost:8080/center/report/update/{reportNo}
    */
    @GetMapping("/update/{reportNo}")
    public String reportUpdateForm(@SessionAttribute("user") MemberDTO user,
            @PathVariable("reportNo") int reportNo,
            Model model) {
        model.addAttribute("memberRole", user.getMemberRole());
        // 1. 해당 글 불러오기
        ReportDTO report = reportService.getReport(reportNo);

        // 2. 본인 글인지 체크 (아닐 경우 접근 차단)
        if (report.getMemberNo() != user.getMemberNo()) {
            return "redirect:/center/report"; // 목록으로 돌려보내기
        }

        // reportDTO → reportForm 으로 변환
        ReportForm form = new ReportForm();
        form.setReportType(report.getReportType());
        form.setReportTitle(report.getReportTitle());
        form.setReportReason(report.getReportReason());

        form.setReportRef(report.getReportRef());
        form.setReportContent(report.getReportContent());
        if(report.getReportEtc()!=null){form.setReportEtc(report.getReportEtc());}
        // form.setReportMail(report.getReportMail());

        // 이미지의 경우 MultipartFile이 아니라 DB에 저장된 경로라 바로 넣을 수는 없고,
        // 수정화면에서는 DB에 저장된 이미지 목록을 별도로 model에 넣어줌
        model.addAttribute("images", reportService.getImages(reportNo)); // DB 조회
        model.addAttribute("reportForm", form);
        model.addAttribute("reportNo", reportNo);

        return "center/reportForm";
    }

    @PostMapping("/update/{reportNo}")
    public String reportUpdate(@SessionAttribute("user") MemberDTO user,
            @PathVariable("reportNo") int reportNo,
            @Valid ReportForm form,
            BindingResult br,
            @RequestParam(value = "deleteImgIds", required = false) List<Integer> deleteImgIds,
            Model model) {
        if (br.hasErrors()) {
            model.addAttribute("reportNo", reportNo);
            model.addAttribute("images", reportService.getImages(reportNo));
            return "center/reportForm";
        }

        // 본인 글 확인
        ReportDTO report = reportService.getReport(reportNo);
        if (report.getMemberNo() != user.getMemberNo()) {
            return "redirect:/center/report";
        }

        // 서비스 호출 (수정 처리)
        reportService.updateReport(reportNo, form, deleteImgIds);

        return "redirect:/center/report/list";
    }

    /** 신고 철회(테이블에서 삭제됨) 
     * localhost:8080/center/report/cancel/{reportNo}
    */
    @PostMapping("/delete/{reportNo}")
    public String deleteReport(@SessionAttribute("user") MemberDTO user, @PathVariable("reportNo") int reportNo) {

        ReportDTO report = this.reportService.getReport(reportNo);
        int memberNo = user.getMemberNo();
        if (report.getMemberNo() != memberNo) {
            return "redirect:/center/report";
        }
        this.reportService.deleteReport(reportNo);
        return "redirect:/center/report";
    }

    @GetMapping("/checkReportRef")
    public ResponseEntity<String> checkReportRef(@RequestParam("reportRef") String reportRef) {
        String result = reportService.checkReportRef(reportRef);
        return ResponseEntity.ok(result); // "MEMBER" | "SHOP" | "NONE"
    }
}
