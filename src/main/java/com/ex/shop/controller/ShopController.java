package com.ex.shop.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ex.admin.model.data.RestrictDTO;
import com.ex.admin.model.service.RestrictService;
import com.ex.member.model.data.MemberDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.repository.OrderDetailRepository;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.repository.OrderRepository;
import com.ex.order.model.service.OrderService;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.data.ProductReviewCommentDTO;
import com.ex.product.model.data.ProductReviewDTO;
import com.ex.product.model.repository.ProductReviewMapper;
import com.ex.product.model.service.ProductReviewService;
import com.ex.product.model.service.ProductService;
import com.ex.shop.model.data.SalesHistoryDTO;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.data.ShopNoticeDTO;
import com.ex.shop.model.data.ShopStatsDTO;
import com.ex.shop.model.repository.SalesHistoryRepository;
import com.ex.shop.model.repository.ShopNoticeRepository;
import com.ex.shop.model.repository.ShopRepository;
import com.ex.shop.model.repository.ShopReviewRepository;
import com.ex.shop.model.service.SalesHistoryService;
import com.ex.shop.model.service.ShopService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * ShopController
 * --------------------------------------------------------------------------------------------------------
 * 판매자 및 상점 관련 컨트롤러
 * 
 * 주요 기능
 * 1. 판매자 메인 페이지 및 상점 등록/수정
 * 2. 유저용 상점 상세 조회
 * 3. 판매자 알림 CRUD + 고정 처리
 * 4. 주문/매출 처리 (주문 수락, 거절, 배송 상태 변경)
 * 5. 리뷰 답글 기능
 */
@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    // ------------------- 서비스 및 레포지토리 의존성 주입 -------------------
    private final ShopService shopService;
    private final ProductService productService;
    private final OrderService orderService;
    private final ShopNoticeRepository shopNoticeRepository;
    private final ProductReviewService productReviewService;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final SalesHistoryService salesHistoryService;
    private final RestrictService restrictService;

    @Autowired
    private SalesHistoryRepository saleshistoryRepository;
    @Autowired
    private ShopReviewRepository shopreviewRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private ProductReviewMapper productReviewMapper; // MyBatis Mapper

    // =====================================================================================================
    // 1. 판매자 메인 페이지 및 상점 등록
    // =====================================================================================================

    /**
     * 판매자 메인 페이지
     * - 로그인 사용자 검증 및 판매자 권한 체크
     * - 상점 생성/조회
     * - 상품 목록, 알림(최신/전체), 매출 내역, 통계 조회
     */
    @GetMapping("/shopmain")
    public String shopMain(@RequestParam(value = "tab", required = false, defaultValue = "info") String tab,
            Model model, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null)
            return "redirect:/member/login";
        if (member.getMemberRole() != 1)
            return "redirect:/";

        // 상점 조회 (없으면 생성)
        ShopDTO shop = shopService.getOrCreateShopByMemberNo(member.getMemberNo());
        if (shop == null)
            return "shop/noShop";

        

        // 상점 세션 저장
        model.addAttribute("shop", shop);
        session.setAttribute("shopNo", shop.getShopNo());

        // 상품 목록
        List<ProductDTO> productList = productService.getProductsByShop(shop.getShopNo());
        model.addAttribute("productList", productList);

        // 공지사항 (최신 + 전체)
        ShopNoticeDTO latestNotice = shopService.getLatestNoticeByShopNo(shop.getShopNo());
        List<ShopNoticeDTO> noticeList = shopService.getAllNoticesByShopNo(shop.getShopNo());
        model.addAttribute("latestNotice", latestNotice);
        model.addAttribute("noticeList", noticeList);

        // 판매 내역
        List<OrderHistoryDTO> allSales = orderService.getAllOrdersByShop(shop.getShopNo());
        model.addAttribute("allSales", allSales);

        // 상세 내역이 들어있는 판매 내역
        // shopNo가져오기
        int shopNo = shopService.getShopNo(member.getMemberNo());

        // 이 가게의 주문내역 가져오기(상세내역의 product까지 저장)
        List<OrderHistoryDTO> allOrders = orderService.getSalesOrders(shopNo);
        model.addAttribute("allOrders", allOrders); // 상세 상품이 포함된 판매 내역 전달

        // 판매 통계
        ShopStatsDTO stats = shopService.getShopStats(shop.getShopNo());
        Map<String, Integer> monthlySalesMap = shopService.getMonthlySalesMap(shop.getShopNo());

        model.addAttribute("stats", stats);
        model.addAttribute("monthlySalesMap", monthlySalesMap);

        // 리뷰 내용 리스트 추가 (판매자용: 문자열 리스트만 표시)
        // List<ProductReviewDTO> reviewList =
        // productReviewMapper.selectReviewsByShop(shop.getShopNo());
        // model.addAttribute("reviewList", reviewList == null ? List.of() :
        // reviewList);

        model.addAttribute("tab", tab);

        // // 제재 여부 확인
        // if (shop != null && shop.getShopStatus() == 1) {
        // int memberNo = member.getMemberNo();
        // boolean isRestrict = this.restrictService.checkRestrict(memberNo); 
        // if(!isRestrict){
        //     model.addAttribute("title", "제재 해제");
		// 	model.addAttribute("msg", "상점 제재가 해제되었습니다.");
		// 	model.addAttribute("icon", "success");
		// 	model.addAttribute("loc", "/shop/shopmain");
        // }
        // }
        return "shop/shopmain";
    }

    /**
     * 판매자 상점 등록 처리
     */
    @PostMapping("/shop/register")
    public String register(@ModelAttribute ShopDTO shop) {
        shopRepository.save(shop);
        return "redirect:/shop/list";
    }

    // =====================================================================================================
    // 2. 유저용 상점 상세 페이지
    // =====================================================================================================

    /**
     * 유저용 상점 상세 보기
     * - 상품, 공지사항(고정/전체) 조회
     */
    @GetMapping("/view/{shopNo}")
    public String viewShop(@PathVariable("shopNo") Integer shopNo, Model model) {
        ShopDTO shop = shopService.getShopByShopNo(shopNo);
        if (shop == null)
            return "redirect:/";

        List<ProductDTO> productList = productService.getProductsByShop(shopNo);
        ShopNoticeDTO pinnedNotice = shopNoticeRepository
                .findFirstByShopNoOrderByPinnedDescCreatedAtDesc(shopNo)
                .orElse(null);
        List<ShopNoticeDTO> noticeList = shopService.getAllNoticesByShopNo(shopNo);
        List<ProductReviewDTO> reviewList = productReviewMapper.selectReviewWithComments(shopNo);

        model.addAttribute("shop", shop);
        model.addAttribute("productList", productList);
        model.addAttribute("pinnedNotice", pinnedNotice);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("reviewList", reviewList == null ? List.of() : reviewList);

        return "shop/shopView";
    }

    // =====================================================================================================
    // 3. 판매자 정보 수정
    // =====================================================================================================

    /**
     * 판매자 정보 수정 페이지
     * - 상점 및 통계 데이터 조회
     */
    @GetMapping("/shopUpdate")
    public String updateShopForm(Model model, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null)
            return "redirect:/member/login";
        if (member.getMemberRole() != 1)
            return "redirect:/";

        ShopDTO shop = (ShopDTO) session.getAttribute("shop");
        if (shop == null) {
            shop = shopService.getMyShopByMemberNo(member.getMemberNo());
            if (shop != null)
                session.setAttribute("shop", shop);
        }
        if (shop == null)
            return "redirect:/";

        // 상점 통계
        int totalOrders = saleshistoryRepository.countByShopNo(shop.getShopNo());
        int totalSales = saleshistoryRepository.totalSalesByShopNo(shop.getShopNo());
        int totalReviews = shopreviewRepository.countReviewsByShopNo(shop.getShopNo());
        double avgRating = shopreviewRepository.avgRatingByShopNo(shop.getShopNo());

        model.addAttribute("shop", shop);
        model.addAttribute("stats", Map.of(
                "totalOrders", totalOrders,
                "totalSales", totalSales,
                "totalReviews", totalReviews,
                "avgRating", avgRating));

        return "shop/shopUpdate";
    }

    /**
     * 판매자 정보 수정 처리
     */
    @PostMapping("/shopUpdate")
    public String updateShop(@ModelAttribute ShopDTO shop, HttpSession session) {
        ShopDTO updated = shopService.updateShopsave(shop);
        session.setAttribute("shop", updated);
        return "redirect:/shop/shopmain?tab=info";
    }

    // =====================================================================================================
    // 4. 알림 (공지사항) CRUD + 고정 처리
    // =====================================================================================================

    @GetMapping("/shopNotice") // 알림 등록 페이지 이동
    public String shopNotice(Model model, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null)
            return "redirect:/member/login";

        ShopDTO shop = shopService.getMyShopByMemberNo(member.getMemberNo());
        if (member.getMemberRole() != 1 || shop == null)
            return "redirect:/";

        model.addAttribute("shop", shop);
        return "shop/shopNotice";
    }

    @PostMapping("/shopNotice") // 알림 저장 처리
    public String saveNotice(@RequestParam("noticeTitle") String noticeTitle,
            @RequestParam("noticeContent") String noticeContent,
            HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        ShopDTO shop = shopService.getMyShopByMemberNo(member.getMemberNo());
        if (shop == null)
            return "redirect:/";

        ShopNoticeDTO shopnoticeDTO = new ShopNoticeDTO();
        shopnoticeDTO.setShopNo(shop.getShopNo());
        shopnoticeDTO.setShopNoticeTitle(noticeTitle);
        shopnoticeDTO.setShopNoticeContent(noticeContent);

        shopService.saveShopNotice(shopnoticeDTO);
        return "redirect:/shop/shopmain";
    }

    @GetMapping("/noticeDetail/{shopNoticeNo}") // 알림 상세보기
    public String noticeDetail(@PathVariable("shopNoticeNo") int shopNoticeNo,
            Model model,
            HttpSession session) {
        // 공지사항 조회
        ShopNoticeDTO notice = shopService.getNoticeById(shopNoticeNo);
        model.addAttribute("notice", notice);

        // 로그인한 사용자 가져오기
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member != null) {
            model.addAttribute("member", member);
        }

        return "shop/noticeDetail";
    }

    @PostMapping("/notice/update")
    public String updateNotice(@ModelAttribute ShopNoticeDTO notice, HttpSession session, RedirectAttributes rt) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null)
            return "redirect:/member/login";

        ShopDTO shop = shopService.getMyShopByMemberNo(member.getMemberNo());
        ShopNoticeDTO existing = shopService.getNoticeById(notice.getShopNoticeNo());
        if (shop == null || existing == null || existing.getShopNo() != shop.getShopNo()
                || member.getMemberRole() != 1) {
            rt.addFlashAttribute("msg", "권한이 없습니다.");
            return "redirect:/shop/shopmain";
        }

        shopService.updateNotice(notice);
        rt.addFlashAttribute("msg", "공지사항이 수정되었습니다.");
        return "redirect:/shop/noticeDetail/" + notice.getShopNoticeNo();
    }

    @GetMapping("/notice/delete/confirm/{shopNoticeNo}") // 알림 삭제 확인 페이지
    public String deleteNoticeConfirm(@PathVariable("shopNoticeNo") int shopNoticeNo, Model model) {
        ShopNoticeDTO notice = shopService.getNoticeById(shopNoticeNo);
        if (notice == null)
            return "redirect:/shop/shopmain";
        model.addAttribute("notice", notice);
        return "shop/shopNoticeDelete";
    }

    @PostMapping("/notice/delete") // 알림 삭제 처리
    public String deleteNotice(@RequestParam("shopNoticeNo") int shopNoticeNo,
            RedirectAttributes redirectAttributes) {
        shopService.deleteNotice(shopNoticeNo);
        redirectAttributes.addFlashAttribute("msg", "공지사항이 삭제되었습니다.");
        return "redirect:/shop/shopmain";
    }

    @PostMapping("/pinNotice") // 알림 고정 처리 (Ajax)
    @ResponseBody
    public Map<String, Object> pinNoticeAjax(HttpSession session,
            @RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        Object shopNoObj = session.getAttribute("shopNo");
        if (shopNoObj == null) {
            result.put("success", false);
            result.put("message", "세션 정보 없음");
            return result;
        }

        int noticeNo = Integer.parseInt(payload.get("shopNoticeNo").toString());
        boolean pinned = Boolean.parseBoolean(payload.get("pinned").toString());

        try {
            shopService.pinNotice((Integer) shopNoObj, noticeNo, pinned);
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    // =====================================================================================================
    // 5. 주문(판매) / 매출 관리
    // =====================================================================================================
    /**
     * 판매자 기준 주문 내역 페이지
     */

    @GetMapping("/orders")
    public String orderList(Model model, HttpSession session) {
        Integer memberNo = (Integer) session.getAttribute("memberNo");
        if (memberNo == null)
            return "redirect:/member/login";

        //
        // memberNo 기준으로 모든 주문 상세 가져오기
        // List<OrderDetailDTO> allSales = orderDetailRepository.findByOrderNo(orderNo);
        /*
         * //주문 상세 출력용 product 대입 for(OrderDetailDTO detail: allSales) { ProductDTO p =
         * productService.getProductByProductNo(detail.getProductNo());
         * detail.setProduct(p); }
         */

        // model.addAttribute("allSales", allOrders);
        model.addAttribute("tab", "orderhistory");
        return "shop/shopmain";
    }

    @PostMapping("/{orderNo}/accept") // 주문 수락 (배송 요청 상태로 변경)
    public String acceptOrder(@PathVariable("orderNo") Integer orderNo) {
        orderHistoryRepository.findById(orderNo).ifPresent(sale -> {
            if (sale.getStatus() == 0) {
                sale.setStatus(2);
                sale.getOrderTime().setRequestedAt(LocalDateTime.now());
                orderHistoryRepository.save(sale);
            }
        });
        return "redirect:/shop/shopmain?tab=delivery";
    }

    @PostMapping("/{orderNo}/reject") // 주문 거절
    public String rejectOrder(@PathVariable("orderNo") Integer orderNo) {
        orderHistoryRepository.findById(orderNo).ifPresent(sale -> {
            if (sale.getStatus() == 0) {
                sale.setStatus(1);
                sale.getOrderTime().setRejectedAt(LocalDateTime.now());
                orderHistoryRepository.save(sale);
            }
        });
        return "redirect:/shop/shopmain?tab=orderhistory";
    }
    /*
     * @PostMapping("/{orderNo}/reject1") // 라이더 배정 (점주 수락 → 라이더 수락) public String
     * rejectOrder1(@PathVariable("orderNo") Integer orderNo) {
     * orderHistoryRepository.findById(orderNo).ifPresent(sale -> { if
     * (sale.getStatus() == 2) { sale.setStatus(3);
     * orderHistoryRepository.save(sale); } }); return
     * "redirect:/shop/shopmain?tab=orderhistory"; }
     * 
     * @PostMapping("/{orderNo}/reject2") // 배송 완료 처리 (픽업 → 완료) public String
     * rejectOrder2(@PathVariable("orderNo") Integer orderNo) {
     * orderHistoryRepository.findById(orderNo).ifPresent(sale -> { if
     * (sale.getStatus() == 4) { sale.setStatus(5);
     * orderHistoryRepository.save(sale); } }); return
     * "redirect:/shop/shopmain?tab=orderhistory"; }
     */

    // 작업자:맹재희, 윤예솔
    @GetMapping("/totalsales") // 총 매출 관리 페이지
    public String totalSalesPage(Model model, HttpSession session) {
        Integer shopNo = (Integer) session.getAttribute("shopNo");
        if (shopNo == null)
            return "redirect:/member/login";
        // 모든 주문 내역 조회
        List<OrderHistoryDTO> allSales = orderService.getAllOrders(shopNo);
        model.addAttribute("allSales", allSales);
        return "shop/totalsales";
    }

    // Status >= 2 인것들로 출력
    // 2: 배송 요청
    // 3: 라이더 배송 수락
    // 4: 라이더 픽업 완료
    // 5: 배송완료
    @GetMapping("/delivery") // 배송 내역 조회 (status == 2)
    @ResponseBody
    public List<OrderHistoryDTO> getDeliveryOrders(HttpSession session) {
        Integer shopNo = (Integer) session.getAttribute("shopNo");
        if (shopNo == null)
            return List.of();

        ShopDTO shop = shopService.getShopByShopNo(shopNo);
        return orderService.getDeliveryOrders(shopNo).stream()
                .filter(order -> order.getStatus() >= 2)
                .toList();
    }

    // =====================================================================================================
    // 6. 상품 상태 변경
    // =====================================================================================================

    @PostMapping("/product/status/update")
    public String updateProductStatus(@RequestParam("productNo") int productNo,
            @RequestParam("status") int status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("loginId");
        boolean success = shopService.updateProductStatus(productNo, status, username);
        if (!success)
            redirectAttributes.addFlashAttribute("msg", "권한이 없습니다.");
        return "redirect:/shop/shopmain";
    }

    // =====================================================================================================
    // 7. 리뷰 댓글 기능 (판매자가 리뷰에 답글 작성, 댓글 조회)
    // =====================================================================================================

    @GetMapping("/shopmain/{shopNo}")
    public String shopMain(@PathVariable int shopNo, Model model) {
        ShopDTO shop = shopService.getShopByShopNo(shopNo);
        if (shop == null)
            return "redirect:/";
        model.addAttribute("shop", shop);

        // DTO 전체를 가져옴 (리뷰 내용만이 아니라 전체 정보)
        List<ProductReviewDTO> reviewList = productReviewMapper.selectReviewsByShop(shopNo);
        model.addAttribute("reviewList", reviewList);

        return "shop/shopmain";
    }

    @GetMapping("/shop/review/{shopNo}")
    public String manageShopReviews(@PathVariable int shopNo, Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpSession session) {

        // 로그인한 멤버 정보 가져오기
        MemberDTO member = (MemberDTO) session.getAttribute("user");

        // 판매자만 접근 가능
        if (member.getMemberRole() != 1) {
            return "redirect:/";
        }

        // shop 정보 조회
        ShopDTO shop = shopService.getShopByShopNo(shopNo);
        if (shop == null)
            return "redirect:/";

        model.addAttribute("shop", shop);

        // 리뷰 목록 조회
        List<ProductReviewDTO> reviewList = productReviewMapper.selectReviewsByShopNo(shopNo);

        // 각 리뷰에 댓글 넣기
        for (ProductReviewDTO review : reviewList) {
            List<ProductReviewCommentDTO> comments = productReviewMapper.selectCommentsByReview(review.getReviewNo());
            review.setComments(comments); // ProductReviewDTO에 comments 필드 필요
        }

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("shopNo", shopNo);

        return "shop/manageReviews"; // 판매자용 리뷰 관리 페이지
    }

    // ================================
    // 8. 리뷰 댓글 작성 페이지 및 저장
    // ================================

    /**
     * 댓글 작성 페이지 이동
     * URL: /shop/review/{reviewNo}/comment
     */
    // 댓글 작성 페이지
    @GetMapping("/review/{shopNo}/comment")
    public String commentForm(@PathVariable("shopNo") Long shopNo, Model model, HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null)
            return "redirect:/member/login";

        model.addAttribute("shopNo", shopNo);
        return "shop/comment_form"; // Thymeleaf 페이지
    }

    /**
     * 댓글 저장 처리
     * URL: /shop/review/{reviewNo}/comment/save
     */
    // 댓글 저장 처리
    /*
     * @PostMapping("/review/{shopNo}/comment/save")
     * public String saveComment(@PathVariable("shopNo") Long shopNo,
     * 
     * @RequestParam("commentContent") String commentContent,
     * HttpSession session) {
     * MemberDTO member = (MemberDTO) session.getAttribute("user");
     * if (member == null) return "redirect:/member/login";
     * 
     * ProductReviewCommentDTO comment = ProductReviewCommentDTO.builder()
     * .reviewNo(null) // PK는 DB에서 자동 증가
     * .shopNo(shopNo) // 리뷰가 속한 상점 번호
     * .content(commentContent) // 댓글 내용
     * .rating(null) // NULL 허용 시 jdbcType 지정 필요
     * .build();
     * 
     * 
     * // Mapper나 Service 호출
     * productReviewService.saveComment(comment);
     * 
     * return "redirect:/shop/shopmain?tab=review"; // 댓글 저장 후 상점 리뷰 탭으로 이동
     * }
     */
    // ================================
    // 9. 정산 내역 조회
    // ================================
    @GetMapping("/{shopNo}/settlements")
    public String settlementHistory(@PathVariable("shopNo") int shopNo, Model model) {

        
    	//배송완료 내역 정산내역 '입금'으로 추가
    	shopService.insertSales(shopNo);
    	List<SalesHistoryDTO> sales = salesHistoryService.applySettlementAndGetHistory(shopNo);
    	
    	//정산내역 조회
    	List<SalesHistoryDTO> salesList = salesHistoryService.getSalesByShop(shopNo);
    	if(salesList!=null && salesList.size()>0) {
    		System.out.println(salesList.size());
    		//정산내역 뷰에 전달
    		model.addAttribute("historyList",salesList);
    	}

        // 정산 내역 조회
        //List<SalesHistoryDTO> historyList = salesHistoryService.getSettlementHistory(shopNo);

        // 뷰에 전달
        //model.addAttribute("historyList", historyList);

        //System.out.println(historyList);


        return "shop/settlehistory"; // Thymeleaf settlement-history.html로 이동
    }

}
