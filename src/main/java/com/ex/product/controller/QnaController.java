package com.ex.product.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ex.member.model.data.MemberDTO;               // 세션에서 로그인 사용자 꺼낼 때 사용 (프로젝트에서 이미 쓰던 타입)
import com.ex.product.model.data.QnaDTO;
import com.ex.product.model.service.QnaService;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class QnaController { // 작업자 : 안성진

    private final QnaService qnaService;

    /* 날짜 포맷 (목록용) */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 상품 상세 페이지: 문의 등록
    // URL : http://localhost:8080/product/{productNo}/qna
    // title, content, secret
    @PostMapping("/{productNo}/qna")
    @ResponseBody
    public ResponseEntity<?> createQna(@PathVariable("productNo") int productNo,
                                       @RequestBody CreateQnaForm form,
                                       HttpSession session) {
        // 로그인 체크: 세션 "user"에 MemberDTO가 들어있다고 프로젝트에서 이미 사용 중
        MemberDTO loginUser = (MemberDTO) session.getAttribute("user");
        if (loginUser == null) {
            // 401로 내려주면 프론트에서 로그인 페이지로 이동 처리 가능
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("status", "login", "msg", "로그인이 필요합니다."));
        }

        // 필수값 검증(아주 기초)
        String title   = safe(form.getTitle());
        String content = safe(form.getContent());
        boolean secret = Boolean.TRUE.equals(form.getSecret());

        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "msg", "내용을 입력해주세요."));
        }

        // 서비스 호출 → QNA 생성 (상품번호로 shopNo를 내부에서 같이 세팅)
        try {
        	int boardNo = qnaService.createQna(productNo,loginUser.getMemberNo(),loginUser.getMemberId(),title,content,secret);        	
        	return ResponseEntity.ok(Map.of("status", "ok", "boardNo", boardNo));
        }catch(IllegalArgumentException iae) {
        	return ResponseEntity.badRequest().body(Map.of("status", "error", "msg", iae.getMessage()));
        }catch(Exception e) {
        	return ResponseEntity.status(500).body(Map.of("status", "error", "msg", "문의 등록 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/{productNo}/qna/form")
    public String createQnaForm(@PathVariable("productNo") int productNo,
    							@ModelAttribute CreateQnaForm form,
    							HttpSession session) {
    	MemberDTO member = (MemberDTO) session.getAttribute("user");
    	if(member == null) {
    		return "redirect:/member/login";
    	}
    	
    	String title = safe(form.getTitle());
    	String content = safe(form.getContent());
    	boolean secret = Boolean.TRUE.equals(form.getSecret());
    	
    	if(content.isBlank()) {
    		return "redirect:/product/detail/" + productNo + "#qna";
    	}
        // 저장
        qnaService.createQna( productNo, member.getMemberNo(), member.getMemberId(), title, content, secret );
        
        return "redirect:/shop/shopmain?tab=qna";
    }
    
    // 상품 상세 페이지: 문의 목록 (무한/더보기)
    // URL : /product/{productNo}/qna
    @GetMapping("/{productNo}/qna")
    @ResponseBody
    public ResponseEntity<?> getProductQna(@PathVariable("productNo") int productNo,
                                           @RequestParam(name="page", defaultValue = "1") int page,
                                           @RequestParam(name="size", defaultValue = "10") int size) {
        QnaService.QnaPage p = qnaService.getProductQnaPage(productNo, page, size);

        // 1페이지이고 데이터가 하나도 없으면 204로 응답 (프론트 코드가 이 경우를 따로 처리함)
        if (p.total() == 0 && page <= 1) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        // 프론트가 기대하는 키로 변환 (renderQnaRow에서 title/writer/createdAt/answered/qnaId 사용)
        List<Map<String, Object>> items = p.items().stream().map(dto -> {
            Map<String, Object> m = new HashMap<>();
            m.put("qnaId", dto.getBoardNo());
            m.put("title", safe(dto.getBoardTitle()));
            m.put("writer", maskId(dto.getMemberId())); // 아이디 마스킹(노출 최소화)
            m.put("createdAt", dto.getReg() != null ? dto.getReg().format(DATE_FMT) : "");
            m.put("answered", dto.getIsAnswered() == 2); // 2=답변완료
            m.put("secret", dto.getStatus() == 3);       // 3=비밀글 (권한에 따라 프론트에서 처리 가능)
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "items",   items,
                "hasMore", p.hasMore(),
                "total",   p.total()
        ));
    }

    // 문의 단건 조회 (상세 페이지나 팝업에서 사용할 수 있음)
    // URL : /product/{productNo}/qna/{boardNo}
    // 응답 뷰 : /product/qna-detail
    // QnA 상세 조회 (Ajax 모달용)
    @GetMapping("/{productNo}/qna/{boardNo}")
    @ResponseBody
    public ResponseEntity<?> getQnaDetail(@PathVariable("productNo") int productNo,
                                          @PathVariable("boardNo") int boardNo) {
        QnaDTO qna = qnaService.getQna(boardNo);
        if (qna == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
            "title", qna.getBoardTitle(),
            "writer", qna.getMemberId(),
            "content", qna.getBoardContent(),
            "createdAt", qna.getReg() != null 
                ? qna.getReg().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "",
            "answered", qna.getIsAnswered() == 2
        ));
    }

    // 문의 삭제
    @PostMapping("/qna/{boardNo}/delete")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable("boardNo") int boardNo) {
        boolean ok = qnaService.delete(boardNo);
        return ok ? ResponseEntity.ok(Map.of("status","ok"))
                  : ResponseEntity.status(500).body(Map.of("status","error"));
    }

    // 비밀글 전환
    @PostMapping("/qna/{boardNo}/secret")
    @ResponseBody
    public ResponseEntity<?> secret(@PathVariable("boardNo") int boardNo) {
        boolean ok = qnaService.secret(boardNo);
        return ok ? ResponseEntity.ok(Map.of("status","ok"))
                  : ResponseEntity.status(500).body(Map.of("status","error"));
    }

    // 답변 여부 변경
    // answered : true/false
    @PostMapping("/qna/{boardNo}/answered")
    @ResponseBody
    public ResponseEntity<?> answered(@PathVariable("boardNo") int boardNo,
                                      @RequestBody AnsweredForm form) {
        boolean ok = qnaService.answered(boardNo, Boolean.TRUE.equals(form.getAnswered()));
        return ok ? ResponseEntity.ok(Map.of("status","ok"))
                  : ResponseEntity.status(500).body(Map.of("status","error"));
    }

    /* ===========================
     * 내부 유틸 & 폼 클래스
     * =========================== */

    /** null -> "" 치환용 */
    private static String safe(String s) { return s == null ? "" : s.trim(); }

    /** 아이디 마스킹 (예: ab****) */
    private static String maskId(String id) {
        if (id == null || id.isBlank()) return "";
        if (id.length() <= 2) return id.charAt(0) + "*";
        return id.substring(0, 2) + "*".repeat(Math.max(1, id.length() - 2));
    }

    /** 문의 생성 요청 바디 */
    @Data
    public static class CreateQnaForm {
        private String title;			// 제목
        private String content;			// 내용
        private Boolean secret;			// 비밀글 여부
    }

    /** 답변 여부 변경 바디 */
    @Data
    public static class AnsweredForm {
        private Boolean answered;
    }
}
