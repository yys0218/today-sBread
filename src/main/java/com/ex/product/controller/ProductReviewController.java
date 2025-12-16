package com.ex.product.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.ex.member.model.data.MemberDTO;
import com.ex.product.model.data.ProductReviewDTO;
import com.ex.product.model.service.ProductReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductReviewController {

	private final ProductReviewService reviewService;
	
	private Integer safeToInt(Object o) {
		if(o == null) return null;
		if(o instanceof Number) return ((Number) o).intValue();
		try {return Integer.parseInt(String.valueOf(o));}catch(Exception e) {return null;}}
	
	// 후기 요약
	// /product/{productNo}/reviews/summary
	// 평균 별점, 총 개수, 히스토그램 반환
	// 상세 페이지 상단 요약 박스에서 사용
	@GetMapping("{productNo}/reviews/summary")
	@ResponseBody
	public ResponseEntity<?> getReviewSummary(@PathVariable("productNo") int productNo){
		try {
			Map<String, Object> sum = reviewService.getSummary(productNo);
			return ResponseEntity.ok(sum);
		}catch(IllegalArgumentException iae) {
			return ResponseEntity.badRequest().body(Map.of("status", "error", "msg", iae.getMessage()));
		}catch(Exception e) {
			return ResponseEntity.status(500).body(Map.of("status", "error", "msg", "서버 오류가 발생했습니다."));
		}
	}
	
	// 후기 목록 (더보기 / 무한스크롤)
	// /product/{productNo}/reviews
	// 파라미터 : page, size, sort, photoOnly
	@GetMapping("{productNo}/reviews")
	@ResponseBody
	public ResponseEntity<?> getReviewPage(@PathVariable("productNo") int productNo,
										   @RequestParam(name="page", defaultValue="1") int page,
										   @RequestParam(name="size", defaultValue="10") int size,
										   @RequestParam(name="sort", defaultValue="recent") String sort,
										   @RequestParam(name="photoOnly", defaultValue="0") int photoOnly){
		// 총 개수
		int total = reviewService.totalByProduct(productNo);
		
		// 1페이지 , 데이터 0 -> 204
		if(total == 0 && page <= 1) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}
		
		// 목록 데이터
		List<ProductReviewDTO> list = reviewService.findPageByProduct(productNo, page, size);
		
		// 프론트 포맷으로 변경
		var items = new java.util.ArrayList<java.util.Map<String, Object>>();
		var DTF = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		
		for(ProductReviewDTO r : list) {
			Map<String, Object> m = new HashMap<>();
			m.put("reviewId", r.getReviewNo()); 	// 리뷰 PK
			m.put("memberId", r.getMemberId());		// 리뷰 쓴 회원 이름
			m.put("rating", r.getRating());			// 1~5
			m.put("content", r.getReviewContent());	// 리뷰 내용
			m.put("createdAtStr", r.getCreateDate() != null ? r.getCreateDate().format(DTF) : "");	// 후기 작성 시간
			m.put("images", java.util.List.of());	// 이미지 기능 (없으면 빈 열)
			items.add(m);
		}
		
		boolean hasMore = page * size < total;
		return ResponseEntity.ok(Map.of(
				"items", items,
				"hasMore", hasMore,
				"total", total
		));
	}
	
	// 후기 프래그먼트(서버사이드 조각)
    // /product/detail/{productNo}/reviews
    @GetMapping("detail/{productNo}/reviews")
    public String reviewListFragment(@PathVariable("productNo") int productNo,
                                     @RequestParam(name="page", defaultValue="1") int page,
                                     @RequestParam(name="size", defaultValue="5") int size,
                                     Model model) {
        int total = reviewService.totalByProduct(productNo);
        List<ProductReviewDTO> rvList = reviewService.findPageByProduct(productNo, page, size);
        boolean hasNext = page * size < total;

        model.addAttribute("rvList", rvList);
        model.addAttribute("rvPage", page);
        model.addAttribute("rvSize", size);
        model.addAttribute("rvHasNext", hasNext);

        // detail.html 안의 rv-list fragment 조각만 렌더링
        return "product/detail :: rv-list";
    }
    
     // 4) 후기 등록(AJAX)
     // /product/reviews
     // 바디 JSON: { productNo, rating, reviewContent }
    @PostMapping("reviews")
    @ResponseBody
    public ResponseEntity<?> addReview(@SessionAttribute(name = "user", required = false) MemberDTO user,
                                       @RequestBody Map<String, Object> body) {
        // 1) 로그인 체크
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("status","login","msg","로그인이 필요합니다."));
        }

        // 2) 파라미터 파싱/검증
        Integer productNo = safeToInt(body.get("productNo"));
        Integer rating = safeToInt(body.get("rating"));
        Integer orderNo = safeToInt(body.get("orderNo"));
        String content = body.get("reviewContent") == null ? "" : String.valueOf(body.get("reviewContent"));

        if(orderNo == null || orderNo <= 0) {
        	return ResponseEntity.badRequest().body(Map.of("status","error","msg","orderNo가 올바르지 않습니다."));
        }
        if (productNo == null || productNo <= 0) {
            return ResponseEntity.badRequest().body(Map.of("status","error","msg","productNo가 올바르지 않습니다."));
        }
        if (content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status","error","msg","후기 내용을 입력해주세요."));
        }
        if (rating == null) rating = 0; // 서비스에서 1~5로 보정

        // 3) DTO 구성
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setOrderNo(orderNo);
        dto.setProductNo(productNo);
        dto.setMemberNo(user.getMemberNo());
        dto.setRating(rating);
        dto.setReviewContent(content);

        // 4) 저장
        try {
            int rows = reviewService.addReview(dto);
            if (rows == 1) {
                return ResponseEntity.ok(Map.of("status","ok"));
            } else {
                return ResponseEntity.status(500).body(Map.of("status","error","msg","저장에 실패했습니다."));
            }
        } catch(org.springframework.dao.DuplicateKeyException dup) {
        	return ResponseEntity.status(409).body(Map.of("status","dup","msg","이미 이 주문에 대해 리뷰를 작성하셨습니다."));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("status","error","msg", iae.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status","error","msg","서버 오류가 발생했습니다."));
        }
    }    
}
