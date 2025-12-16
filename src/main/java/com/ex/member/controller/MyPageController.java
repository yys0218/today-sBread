package com.ex.member.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;      // 리스트 생성용
import java.util.HashMap;        
import java.util.List;
import java.util.Map;            // Map 인터페이스
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity; // JSON 응답 구성용
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.service.MyPageService;
import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.repository.OrderDetailRepository;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.service.ShoppingCartService;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.data.WishDTO;
import com.ex.product.model.service.ProductService;
import com.ex.product.model.service.QnaService.QnaPage;
// (후기 기능을 사용한다면 주입될 서비스/DTO 임포트)
// 프로젝트에 실제로 존재해야 합니다. 없다면 이 두 임포트와 관련 메서드를 주석 처리하세요.
import com.ex.product.model.data.ProductReviewDTO;
import com.ex.product.model.data.QnaDTO;
import com.ex.product.model.service.ProductReviewService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 컨트롤러
 * - 주문 내역/찜 목록/후기 등록 등 마이페이지 관련 화면과 액션 담당
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/member/*") // 기본 URL : http://localhost:8080/member/
public class MyPageController {

    private final ShoppingCartService shoppingCartService;

    private final MyPageService myPageService;
    private final OrderDetailRepository orderDetailRepository; // 주문 상세 CRUD
    private final ProductService productService;
    private final OrderHistoryRepository orderHistoryRepository;
    
    // 후기 저장에 사용할 서비스
    private final ProductReviewService reviewService;


    /**
     * 주문 내역 페이지
     */
    @GetMapping("order-list")
    public String orderHistory(@RequestParam(name="page", defaultValue="1") int page,
    						   @RequestParam(name="size", defaultValue="5") int size,
    						   HttpSession session, Model model) {
    	
        // 로그인 확인 (세션에 user가 없으면 로그인 페이지로)
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null) {
            return "/member/login";
        }
        
        // 페이징 처리
        PageRequest pr = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "orderNo"));
        
        // 리포지토리 호출 (OrderHistoryRepository : findByMember)
        Page<OrderHistoryDTO> pageResult = orderHistoryRepository.findByMember(member, pr);
        
        // 현재 페이지 주문
        List<OrderHistoryDTO> orderList = pageResult.getContent();
        

        // 각 주문에 첫 상품 정보 채워넣기 (화면 표시용)
        for (OrderHistoryDTO order : orderList) {
            Optional<List<OrderDetailDTO>> opt = orderDetailRepository.findByOrder(order);
            List<OrderDetailDTO> details = opt.orElseGet(ArrayList::new);
            order.setOrderDetail(details);

            if (!details.isEmpty()) {
                ProductDTO product = productService.product(details.get(0).getProductNo());
                order.setProduct(product);
            }
        }
        
        // 페이징 세팅
        int currentPage = pageResult.getNumber() + 1;
        int totalPages = Math.max(1, pageResult.getTotalPages());
        boolean hasPrev = currentPage > 1;
        boolean hasNext = currentPage < totalPages;

        model.addAttribute("orderList", orderList);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("prevPage", hasPrev ? currentPage - 1 : 1);
        model.addAttribute("nextPage", hasNext ? currentPage + 1 : totalPages);
        
        return "member/order-list";
    }
    
    // 주문 내역 - 주문 상세
    @GetMapping("order-detail/{orderNo}")
    public String orderDetail(@PathVariable("orderNo") int orderNo,
    						  @RequestParam(name="fragment", defaultValue="false") boolean fragment,
    						  HttpSession session, Model model, RedirectAttributes ra) {
    	MemberDTO member = (MemberDTO) session.getAttribute("user");
    	if(member == null) return "/member/login";
    	
    	// 주문 본인 소유 검증 + shop/orderTime 즉시 로딩
    	Optional<OrderHistoryDTO> optOrder = orderHistoryRepository.findDetailForMember(orderNo, member.getMemberNo());
    	
    	if(optOrder.isEmpty()) {
    		ra.addFlashAttribute("error", "주문을 찾을 수 없습니다.");
    		return "redirect:/member/order-list";
    	}
    	
    	OrderHistoryDTO order = optOrder.get();
    	
    	// 상세 정보 로딩
    	List<OrderDetailDTO> items = orderDetailRepository.findByOrderNo(orderNo);
    	
    	// 각 아이템에 Product 붙이기
    	for(OrderDetailDTO od : items) {
    		ProductDTO p = productService.product(od.getProductNo());
    		if(p != null) od.setProduct(p);
    	}
    	
    	// 합계 계산
    	int productTotal = items.stream().mapToInt(od -> od.getPrice() * od.getQuantity()).sum();
    	int deliveryFee = order.getDeliveryFee();
    	int grandTotal = productTotal + deliveryFee;
    	
    	String statusText = switch(order.getStatus()) {
			case 0 -> "결제 완료";
			case 1 -> "주문 거절";
			case 2 -> "주문 수락";
			case 3 -> "배송 대기";
			case 4 -> "픽업 완료";
			case 5 -> "배송 완료";
			default -> "주문 취소";
    	};
    	
    	model.addAttribute("order", order);
    	model.addAttribute("items", items);
    	model.addAttribute("statusText", statusText);
    	model.addAttribute("productTotal", productTotal);
    	model.addAttribute("deliveryFee", deliveryFee);
    	model.addAttribute("grandTotal", grandTotal);
    	
    	if(fragment) return "member/order-detail-modal :: od-modal";
    	
    	return "member/order-detail-modal";
    }


	/**
     * 찜한 상품 페이지 (페이징)
     */
    @GetMapping("wish-list")
    public String wishListPage(@RequestParam(name = "page", defaultValue = "1") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size,
                               HttpSession session, Model model) {

        // 1) 로그인 확인
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null) return "/member/login";

        // 2) 찜 목록 + 전체 개수
        List<WishDTO> wishes = myPageService.getMemberWishPage(member, page, size);
        int total = myPageService.countMemberWishList(member);

        // 3) productNo -> ProductDTO 매핑
        Map<Integer, ProductDTO> productMap = new HashMap<>();
        for (WishDTO w : wishes) {
            ProductDTO p = productService.product(w.getProductNo());
            productMap.put(w.getProductNo(), p);
        }

        // 4) 페이징 플래그
        boolean hasPrev = page > 1;
        boolean hasNext = page * size < total;

        // 5) 모델에 담아 뷰로 전달
        model.addAttribute("wishes", wishes);
        model.addAttribute("productMap", productMap);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("total", total);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);
        return "member/wish-list";
    }

    /**
     * 찜 삭제(단일)
     */
    @PostMapping("wish-delete-one")
    public String deleteWishOne(@RequestParam("wishNo") int wishNo,
                                @RequestParam(name = "page", defaultValue = "1") int page,
                                @RequestParam(name = "size", defaultValue = "10") int size,
                                HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null) return "/member/login";
        myPageService.deleteWishOne(member, wishNo);
        return "redirect:/member/wish-list?page=" + page + "&size=" + size;
    }

    /**
     * 찜 삭제(복수)
     */
    @PostMapping("wish-delete-bulk")
    public String deleteWishBulk(@RequestParam(name = "wishNos", required = false) List<Integer> wishNos,
                                 @RequestParam(name = "page", defaultValue = "1") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                 HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null) return "/member/login";
        myPageService.deleteWishBulk(member, wishNos);
        return "redirect:/member/wish-list?page=" + page + "&size=" + size;
    }
    
    /**
     	찜 해당 상품 -> 장바구니로 이동
    */
/*    @PostMapping("wish-add-cart")
    public String addWishToCart(@RequestParam("productNo") int productNo,
    							@RequestParam(name = "qty", defaultValue = "1") int qty,
    							@RequestParam(name = "returnUrl", required = false) String returnUrl,
    							HttpSession session, RedirectAttributes ra) {
    	// 로그인 확인
    	MemberDTO member = (MemberDTO) session.getAttribute("user");
    	if(member == null) {
    		return "redirect:/member/login";
    	}
    	
    	// 수량 보정
    	int safeQty = Math.max(1, qty);
    	
    	try {
    		// 장바구니 담기 작업을 MyPageService에서 수행함
    		myPageService.addToCart(member.getMemberNo(), productNo, safeQty);
    		
    		ra.addFlashAttribute("msg", "장바구니에 담았습니다.");
    	}catch(Exception e) {
    		ra.addFlashAttribute("error","장바구니 담기에 실패했습니다.");
    	}
    	
    	// 원래 페이지로 복귀
    	String back = StringUtils.hasText(returnUrl) ? returnUrl : "/member/wish-list";
    	return "redirect:" + back;
    }
*/
    // 장바구니 추가
    @PostMapping("wish-add-cart")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> AjaxAddCart(@RequestBody Map<String, Object> body,
    													   HttpSession session){
    	MemberDTO member = (MemberDTO) session.getAttribute("user");
    	if(member == null ) {
    		return ResponseEntity.status(401).body(Map.of("status","login"));
    	}
    	
    	Integer productNo = toInt(body.get("productNo"));
        Integer qty       = toInt(body.get("qty"));
        int safeQty = Math.max(1, qty == null ? 1 : qty);

        if (productNo == null || productNo <= 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("status","error","msg","productNo가 올바르지 않습니다."));
        }

        try {
            myPageService.addToCart(member.getMemberNo(), productNo, safeQty);
            return ResponseEntity.ok(Map.of("status","ok"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("status","error","msg","장바구니 담기 실패"));
        }
    }
    
    // ==========================
    //   후기 등록 (AJAX)
    // ==========================
    
    @GetMapping("review-list")
    public String reviewList(@RequestParam(name="page", defaultValue = "1") int page,
    						 @RequestParam(name="size", defaultValue = "5") int size,
    						 HttpSession session, Model model) {
    	MemberDTO member = (MemberDTO) session.getAttribute("user");
    	if(member == null) return "/member/login";
    	
    	// 서비스에서 회원 후기 목록을 불러옴
    	PageRequest pr = PageRequest.of(Math.max(0, page-1), Math.max(1, size), Sort.by("createDate").descending());
    	Page<ProductReviewDTO> reviews = reviewService.findPageByMember(member.getMemberNo(), pr);
    	
    	model.addAttribute("reviews", reviews);
    	model.addAttribute("page", page);
    	model.addAttribute("size", size);
    	return "member/review-list";
    }
    /**
     * 후기 등록
     * 프론트 예시:
     * $.post('/member/review', { productNo, reviewContent, rating })
     */
    @PostMapping("review")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addReviewForm(@RequestParam("productNo") Integer productNo,
                                                             @RequestParam("orderNo") Integer orderNo,
                                                             @RequestParam("reviewContent") String reviewContent,
                                                             @RequestParam(name = "rating", defaultValue = "0") Integer rating,
                                                             HttpSession session){
        Map<String, Object> res = new HashMap<>();

        // 로그인 확인
        MemberDTO user = (MemberDTO) session.getAttribute("user");
        if (user == null) {
            res.put("status", "login");
            res.put("msg", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(res);
        }

        // 파라미터 1차 검증
        if(orderNo == null || orderNo <= 0) {
            res.put("status", "error");
            res.put("msg", "orderNo가 올바르지 않습니다.");
            return ResponseEntity.badRequest().body(res);
        }
        if (productNo == null || productNo <= 0) {
            res.put("status", "error");
            res.put("msg", "productNo가 올바르지 않습니다.");
            return ResponseEntity.badRequest().body(res);
        }

        reviewContent = (reviewContent == null) ? "" : reviewContent.trim();
        if (reviewContent.isEmpty()) {
            res.put("status", "error");
            res.put("msg", "후기 내용을 입력해주세요.");
            return ResponseEntity.badRequest().body(res);
        }

        int safeRating = (rating == null) ? 0 : rating;
        safeRating = Math.max(1, Math.min(5, safeRating));

        // 본인 주문 + 주문 상태=5(배송 완료) 확인
        Optional<OrderHistoryDTO> optOrder =
                orderHistoryRepository.findByOrderNoAndMemberMemberNo(orderNo, user.getMemberNo());
        if (optOrder.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("status","error","msg","주문을 찾을 수 없습니다."));
        }
        OrderHistoryDTO order = optOrder.get();

        // case=5(배송 완료)만 허용
        if (order.getStatus() != 5) {
            return ResponseEntity.status(403).body(Map.of("status","forbidden","msg","배송 완료된 주문에 대해서만 후기를 작성할 수 있습니다."));
        }

        // 해당 주문에 productNo가 실제로 포함되어 있는지 확인
        List<OrderDetailDTO> items = orderDetailRepository.findByOrderNo(orderNo);
        boolean contains = items != null && items.stream().anyMatch(od -> od.getProductNo() == productNo);
        if (!contains) {
            return ResponseEntity.badRequest().body(Map.of("status","error","msg","해당 주문에 포함되지 않은 상품입니다."));
        }

        // 상품의 shopNo 조회 (리뷰 저장에 필요)
        Integer shopNo = null;
        try {
            ProductDTO product = productService.product(productNo);
            if(product != null) shopNo = product.getShopNo();
        } catch(Exception ignore) {}

        if(shopNo == null) {
            res.put("status", "error");
            res.put("msg", "상품의 상점 정보를 찾을 수 없습니다.");
            return ResponseEntity.badRequest().body(res);
        }

        // DTO 구성
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setProductNo(productNo);
        dto.setMemberNo(user.getMemberNo());
        dto.setShopNo(shopNo);
        dto.setOrderNo(orderNo);
        dto.setRating(safeRating);
        dto.setReviewContent(reviewContent);

        // 저장 시도
        try {
            int rows = reviewService.addReview(dto);

            if (rows == 1) {
                res.put("status", "ok");
                return ResponseEntity.ok(res);
            } else {
                res.put("status", "error");
                res.put("msg", "저장에 실패했습니다.");
                return ResponseEntity.internalServerError().body(res);
            }
        } catch(org.springframework.dao.DuplicateKeyException dup) {
            return ResponseEntity.status(409).body(Map.of("status", "dup", "msg", "이미 이 주문에 대해 리뷰를 작성하셨습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("msg", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(res);
        }
    }
    
    // 마이페이지 - 주문 내역 - 후기 작성 모달창
    @GetMapping("order-review-modal")
    public String orderReviewModal(@RequestParam(name="productNo") int productNo,
						           @RequestParam(name="orderNo")   int orderNo,
						           @RequestParam(name="productName", required=false) String productName,
						           @RequestParam(name="fragment", defaultValue="false") boolean fragment,
						           HttpSession session, Model model){
    	
	MemberDTO member = (MemberDTO) session.getAttribute("user");
	if(member == null) return "/member/login";
	
	model.addAttribute("productNo", productNo);
	model.addAttribute("orderNo", orderNo);
	model.addAttribute("productName", (productName==null||productName.isBlank()) ? "상품" : productName);
	
	if(fragment) return "member/order-review-modal :: rv-modal";
	return "member/order-review-modal";
	}
    
    // 마이페이지 - 주문 내역 - 판매자 문의 모달창
    @GetMapping("order-qna-modal")
    public String orderQnaModal(@RequestParam(name="productNo", required = false) int productNo,
    							@RequestParam(name="orderNo", required = false) int orderNo,
    							@RequestParam(name="productName", required = false) String productName,
    							@RequestParam(name="shopName", required = false) String shopName,
    							@RequestParam(name="fragment", defaultValue = "false") boolean fragment,
    							HttpSession session, Model model) {
    	MemberDTO member = (MemberDTO) session.getAttribute("user");
    	if(member == null) return "/member/login";
    	
    	// 모델 바인딩
    	model.addAttribute("productNo", productNo);
    	model.addAttribute("orderNo", orderNo);
    	model.addAttribute("productName", productName);
    	model.addAttribute("shopName", shopName);
    	
    	if(fragment) {
    		return "member/order-qna-modal :: qna-modal";
    	}
    	
    	return "member/order-qna-modal";
    }

    // 정기 결제 : 조회 페이지 (페이징 지원 버전)
    @GetMapping("subscription")
    public String subscriptionPage(@RequestParam(name="page", defaultValue="1") int page,   // 현재 페이지 (기본값 1)
                                   @RequestParam(name="size", defaultValue="5") int size,   // 한 페이지에 보여줄 개수 (기본값 5)
                                   HttpSession session, 
                                   Model model) {
        // 1) 로그인 확인
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null) return "/member/login";

        // 2) PageRequest 생성 (page는 0부터 시작하므로 -1 처리)
        PageRequest pr = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "orderNo"));

        // 3) JPA Repository 호출 (Page<OrderHistoryDTO> 반환)
        Page<OrderHistoryDTO> subsPage = orderHistoryRepository.findSubscriptionByMember(member, pr);

        // 4) 실제 구독 데이터 꺼내기
        List<OrderHistoryDTO> subs = subsPage.getContent();

        // 5) 각 구독에 대표 상품/이미지/이름/다음 결제일 보정 작업
        for (OrderHistoryDTO o : subs) {
            // 주문 상세 조회 후 세팅
            List<OrderDetailDTO> details = orderDetailRepository.findByOrderNo(o.getOrderNo());
            o.setOrderDetail(details);

            // 대표 상품 세팅
            if (!details.isEmpty()) {
                ProductDTO p = productService.product(details.get(0).getProductNo());
                o.setProduct(p);

                // 구독 이름이 비어있으면 상품명으로 대체
                if (o.getName() == null || o.getName().isBlank()) {
                    if (p != null) o.setName(p.getProductName());
                }
            }

            // 다음 결제일 계산 (orderTime + deliveryCycle 기반)
            if (o.getNextPaymentAt() == null && o.getOrderTime() != null) {
                LocalDateTime orderedAt = o.getOrderTime().getOrderedAt();
                if (orderedAt != null && o.getDeliveryCycle() != null) {
                    o.setNextPaymentAt(orderedAt.plusDays(o.getDeliveryCycle().getDayOfMonth()));
                }
            }
        }

        // 6) 페이징 관련 변수 (현재 페이지, 전체 페이지 수, 이전/다음 여부)
        int currentPage = subsPage.getNumber() + 1;             // JPA는 0-based, 화면은 1부터
        int totalPages = Math.max(1, subsPage.getTotalPages()); // 최소 1
        boolean hasPrev = currentPage > 1;                      // 이전 페이지 여부
        boolean hasNext = currentPage < totalPages;             // 다음 페이지 여부

        // 7) 뷰에 전달할 데이터 바인딩
        model.addAttribute("subs", subs);              // 구독 목록
        model.addAttribute("page", currentPage);       // 현재 페이지
        model.addAttribute("size", size);              // 페이지 크기
        model.addAttribute("totalPages", totalPages);  // 전체 페이지 수
        model.addAttribute("hasPrev", hasPrev);        // 이전 버튼 여부
        model.addAttribute("hasNext", hasNext);        // 다음 버튼 여부
        model.addAttribute("prevPage", hasPrev ? currentPage - 1 : 1);             // 이전 페이지 번호
        model.addAttribute("nextPage", hasNext ? currentPage + 1 : totalPages);    // 다음 페이지 번호

        // 8) subscription.html 뷰로 이동
        return "member/subscription";
    }

    
    // 정기 결제 : 해지
    @PostMapping("subscription/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelSubscription(@RequestBody Map<String, Object> body, HttpSession session){
    	MemberDTO member = (MemberDTO) session.getAttribute("user");
    	if(member == null) return ResponseEntity.status(401).body(Map.of("status","login"));
    	
    	Integer orderNo = toInt(body.get("orderNo"));
        if (orderNo == null || orderNo <= 0) {
            return ResponseEntity.badRequest().body(Map.of("status","error","msg","orderNo가 올바르지 않습니다."));
        }

        // 본인 소유 + 단건 조회
        var opt = orderHistoryRepository.findByOrderNoAndMemberMemberNo(orderNo, member.getMemberNo());
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("status","error","msg","구독을 찾을 수 없습니다."));
        }
        OrderHistoryDTO order = opt.get();

        if (order.getCustomerUid() == null || order.getCustomerUid().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("status","error","msg","이미 해지된 구독입니다."));
        }

        try {
            shoppingCartService.cancelSubscription(order);
            return ResponseEntity.ok(Map.of("status","ok"));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(502).body(Map.of("status","error","msg", ise.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status","error","msg","구독 해지 중 오류가 발생했습니다."));
        }
    }
    // ===== 유틸 =====
    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch(Exception e){ return null; }
    }
    
    // 마이페이지 - 상품 문의 페이지
    @GetMapping("product-qna")
    public String productQnaPage(@RequestParam(name="page", defaultValue="1") int page,
                                 @RequestParam(name="size", defaultValue="5") int size,
                                 @RequestParam(name="status", required = false) String status,
                                 @RequestParam(name="q", required = false) String q,
                                 HttpSession session, Model model) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if(member == null) return "/member/login";

        QnaPage qnaPage = myPageService.getMyQnaPage(member.getMemberNo(), page, size, q, status);

        // 총 페이지 계산
        int totalPages = (int) Math.ceil((double) qnaPage.total() / qnaPage.size());
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        model.addAttribute("qnas", qnaPage.items());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("hasPrev", page > 1);
        model.addAttribute("hasNext", page < totalPages);
        model.addAttribute("q", q);
        model.addAttribute("status", status);

        return "member/product-qna";
    }
}