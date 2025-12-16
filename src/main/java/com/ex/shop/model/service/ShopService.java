package com.ex.shop.model.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.member.model.data.MemberDTO;
import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.repository.OrderRepository;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.repository.ProductMapper;
import com.ex.shop.model.data.SalesHistoryDTO;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.data.ShopNoticeDTO;
import com.ex.shop.model.data.ShopStatsDTO;
import com.ex.shop.model.repository.SalesHistoryRepository;
import com.ex.shop.model.repository.ShopNoticeRepository;
import com.ex.shop.model.repository.ShopOrderHistoryRepository;
import com.ex.shop.model.repository.ShopRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * ShopService
 * --------------------------------------------------------------------------------------------------------
 * 판매자의 상점 관련 비즈니스 로직 담당 서비스
 * - 상점 정보 CRUD
 * - 판매자 페이지 알림사항 CRUD
 * - 판매자 통계 조회
 * - 판매자 상품 상태 변경
 *
 * @Service : 스프링이 서비스 컴포넌트로 감지 / 관리
 * @RequiredArgsConstructor : final 필드 생성자 주입
 */
@Service
@RequiredArgsConstructor
public class ShopService {

    // ======================= Repository (생성자 주입) =======================
    private final ShopRepository shopRepository; // 상점 CRUD
    private final ShopNoticeRepository shopNoticeRepository; // 판매자 페이지 알림사항 CRUD
    private final ShopOrderHistoryRepository shopOrderHistoryRepository; // 판매자 주문내역 조회
    private final OrderHistoryRepository orderHistoryRepository; //판매 내역 조회

    // ======================= 기타 의존성 (필드 주입) =======================
    @Autowired
    private SalesHistoryRepository salesHistoryRepository; // 매출 내역 조회
    @Autowired
    private ProductMapper productMapper; // 상품 관련 Mapper
    @Autowired
    private OrderRepository orderRepository;
    

    // ======================= 상점(Shop) 관련 =======================

    /**
     * 신규 상점 등록
     */
    public void insertShop(int memberNo, String tinNo, String businessName,
            LocalDate businessOpenAt, String businessContact,
            String businessMail, String businessBank,
            String businessAccName, String businessAccNum) {

        ShopDTO shop = new ShopDTO();
        shop.setMemberNo(memberNo);
        shop.setTinNo(tinNo);
        shop.setBusinessName(businessName);
        shop.setBusinessOpenAt(businessOpenAt);
        shop.setBusinessContact(businessContact);
        shop.setBusinessMail(businessMail);
        shop.setBusinessBank(businessBank);
        shop.setBusinessAccName(businessAccName);
        shop.setBusinessAccNum(businessAccNum);
        shop.setShopRegAt(LocalDateTime.now());

        shopRepository.save(shop);
    }

    /**
     * memberNo 기준 상점 조회, 없으면 기본값으로 새 상점 생성
     */
    public ShopDTO getOrCreateShopByMemberNo(int memberNo) {
        ShopDTO shop = shopRepository.findTopByMemberNoAndShopRegResult(memberNo, "Y");
        if (shop == null) {
            shop = new ShopDTO();
            shop.setMemberNo(memberNo);
            shop.setShopName("내 상점");
            shop.setShopRegAt(LocalDateTime.now());
            shop.setShopStatus(0); // 정상운영
            shopRepository.save(shop);
        }
        return shop;
    }

    /**
     * memberNo로 shopNo 조회
     */
    public int getShopNo(int memberNo) {
        ShopDTO dto = shopRepository.findTopByMemberNoAndShopRegResult(memberNo, "Y");
        return dto.getShopNo();
    }

    /**
     * memberNo 기준 상점 조회
     */
    public ShopDTO getMyShopByMemberNo(int memberNo) {
        return shopRepository.findTopByMemberNoAndShopRegResult(memberNo, "Y");
    }

    /**
     * 세션 기반 상점 조회 (로그인한 회원)
     */
    public ShopDTO getMyShopBySession(HttpSession session) {
        MemberDTO member = (MemberDTO) session.getAttribute("user");
        if (member == null)
            return null;
        return shopRepository.findTopByMemberNoAndShopRegResult(member.getMemberNo(), "Y");
    }

    /**
     * shopNo 기준 상점 조회
     */
    public ShopDTO getMyShopByShopNo(Integer shopNo) {
        return shopRepository.findById(shopNo)
                .orElseThrow(() -> new RuntimeException("상점을 찾을 수 없습니다."));
    }

    /**
     * 판매자 정보 수정 (기존 상점 덮어쓰기, 신규 shopNo 생성 X)
     */
    @Transactional
    public ShopDTO updateShopsave(ShopDTO shop) {
        if (shop.getShopNo() == null)
            throw new IllegalArgumentException("shopNo가 존재하지 않습니다.");

        ShopDTO existing = shopRepository.findById(shop.getShopNo())
                .orElseThrow(() -> new RuntimeException("상점을 찾을 수 없습니다."));

        existing.setShopName(shop.getShopName());
        existing.setShopAddress(shop.getShopAddress());
        existing.setShopContact(shop.getShopContact());
        existing.setOpenTime(shop.getOpenTime());
        existing.setCloseTime(shop.getCloseTime());
        
        System.out.println(shop.getOpenTime());
        
        return shopRepository.save(existing);
    }

    // ======================= 판매자 주문내역 =======================

    /**
     * 판매자 기준 주문내역 조회
     */
    public List<OrderHistoryDTO> getOrdersBySeller(int memberNo) {
        ShopDTO shop = shopRepository.findTopByMemberNoAndShopRegResult(memberNo, "Y");
        if (shop == null)
            throw new RuntimeException("상점이 존재하지 않습니다.");

        List<OrderHistoryDTO> orders = shopOrderHistoryRepository.findByShop(shop);

        for (OrderHistoryDTO order : orders) {
            List<OrderDetailDTO> details = orderRepository.findOrderDetailsByOrderNo(order.getOrderNo());

            // ProductDTO 채우기
            for (OrderDetailDTO od : details) {
                ProductDTO product = productMapper.findById(od.getProductNo());
                od.setProduct(product);
            }

            // 실제로 order에 세팅
            order.setOrderDetail(details);

            // 디버깅
            System.out.println("Order " + order.getOrderNo() + " has " + details.size() + " products.");
        }

        return orders;
    }

    // ======================= 알림사항(ShopNotice) 관련 =======================

    /**
     * 알림사항 저장 (신규 / 수정)
     */
    public void saveShopNotice(ShopNoticeDTO notice) {
        shopNoticeRepository.save(notice);
    }

    /**
     * 상점 번호 기준 최신 공지 조회
     * pinned 우선, 없으면 최신 작성 공지
     */
    public ShopNoticeDTO getLatestNoticeByShopNo(Integer shopNo) {
        return shopNoticeRepository
                .findFirstByShopNoOrderByPinnedDescCreatedAtDesc(shopNo)
                .orElse(null);
    }

    /**
     * 상점 번호 기준 전체 공지 조회 (최신순)
     */
    public List<ShopNoticeDTO> getAllNoticesByShopNo(int shopNo) {
        return shopNoticeRepository.findAllByShopNoOrderByCreatedAtDesc(shopNo);
    }

    /**
     * 공지 단건 조회 (예외 발생)
     */
    public ShopNoticeDTO getNoticeById(int shopNoticeNo) {
        return shopNoticeRepository.findById(shopNoticeNo)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
    }

    /**
     * 공지 수정 (변경감지 사용)
     */
    @Transactional
    public void updateNotice(ShopNoticeDTO notice) {
        ShopNoticeDTO existing = shopNoticeRepository.findById(notice.getShopNoticeNo())
                .orElseThrow(() -> new IllegalArgumentException("알림사항 없음"));

        existing.setShopNoticeTitle(notice.getShopNoticeTitle());
        existing.setShopNoticeContent(notice.getShopNoticeContent());
        existing.setShopAlert(notice.getShopAlert());
    }

    /**
     * 공지 삭제
     */
    public void deleteNotice(int shopNoticeNo) {
        shopNoticeRepository.deleteById(shopNoticeNo);
    }

    /**
     * 공지 고정 상태 업데이트 (boolean)
     */
    @Transactional
    public void updatePinnedStatus(int shopNoticeNo, boolean pinned) {
        ShopNoticeDTO notice = shopNoticeRepository.findById(shopNoticeNo)
                .orElseThrow(() -> new RuntimeException("Notice not found"));
        notice.setPinned(pinned);
        shopNoticeRepository.save(notice);
    }

    /**
     * 공지 고정 상태 업데이트 (int 1=고정, 0=해제)
     */
    public void updatePinnedStatus(int shopNoticeNo, int i) {
        updatePinnedStatus(shopNoticeNo, i == 1);
    }

    /**
     * 특정 상점 내 공지 고정 (1개만 고정 가능)
     */
    @Transactional
    public void pinNotice(int shopNo, int noticeNo, boolean pinned) {
        if (pinned) {
            List<ShopNoticeDTO> notices = shopNoticeRepository.findAllByShopNoOrderByCreatedAtDesc(shopNo);
            for (ShopNoticeDTO n : notices)
                n.setPinned(false);

            ShopNoticeDTO notice = shopNoticeRepository.findByShopNoticeNoAndShopNo(noticeNo, shopNo)
                    .orElseThrow(() -> new RuntimeException("공지 없음"));
            notice.setPinned(true);

            shopNoticeRepository.saveAll(notices);
        } else {
            ShopNoticeDTO notice = shopNoticeRepository.findByShopNoticeNoAndShopNo(noticeNo, shopNo)
                    .orElseThrow(() -> new RuntimeException("공지 없음"));
            notice.setPinned(false);
            shopNoticeRepository.save(notice);
        }
    }

    // ======================= 상품(Product) 상태 변경 =======================

    /**
     * 상품 상태 업데이트
     */
    public boolean updateProductStatus(int productNo, int status, String username) {
        ProductDTO product = productMapper.findById(productNo);
        if (product == null)
            return false;
        int updated = productMapper.updateStatus(productNo, status);
        return updated > 0;
    }

    // ======================= 매출/통계(Sales) 관련 =======================

    /**
     * shopNo 기준 최근 매출 단건 조회
  			>> salesHistoryRepository에 해당 메서드 없음
     */
   // public Optional<SalesHistoryDTO> getSalesByShop(Integer shopNo) {
   //     return salesHistoryRepository.findTopByShopNoOrderByCreatedAtDesc(shopNo);
   // }

    /**
     * shopNo 기준 매출 목록 조회
     */
    public List<SalesHistoryDTO> getSalesListByShop(Integer shopNo) {
        return salesHistoryRepository.findByShopNoOrderByCreatedAtDesc(shopNo);
    }
    

    // ======================= 편의 메서드 =======================

    //배송완료된 주문이 salesHistory에 insert됨
    public void insertSales(int shopNo) {
    		ShopDTO shop = shopRepository.findByShopNo(shopNo);
        	//배송완료된 주문 가져오기
        	List<OrderHistoryDTO> list = orderHistoryRepository.findByShopAndStatus(shop,5);
        	System.out.println("크기"+list.size());
        	//SalesHistory에 insert
        	for(OrderHistoryDTO order : list) {
        		//정산내역에 들어갈 객체 생성
        		SalesHistoryDTO sales = new SalesHistoryDTO();
        		int orderNo = order.getOrderNo();
        		//주문번호를 가진 결과가 있는지 확인
        		Optional<SalesHistoryDTO> _dto = salesHistoryRepository.findByOrderNo(orderNo);
        		if(!(_dto.isPresent())) { //이미 저장된 값이 아니라면
        			sales.setOrderNo(orderNo);
        			sales.setShopNo(order.getShop().getShopNo());
        			sales.setSalesType(1); //1. 입금 2. 환불 3. 정산
        			//배송비 제외한 가게 매출 set
        			sales.setSalesAmount(order.getOrderPrice()-order.getDeliveryFee());
        			System.out.println("금액:"+sales.getSalesAmount());
        			//잔액 조회, 저장
        			//가장 최근 잔액
        			int balance=0;
        			Optional<SalesHistoryDTO> _balance = salesHistoryRepository.findTop1ByshopNoOrderByCreatedAtDesc(order.getShop().getShopNo()); 
        			if(_balance.isPresent()) {
        				balance=_balance.get().getSalesBalance();
        				System.out.println("현재 잔액:"+balance);
        			}
        			// 현재 잔액 + 방금 set한 매출금액
        			sales.setSalesBalance(balance+(sales.getSalesAmount())); 
        			sales.setCreatedAt(LocalDateTime.now());
        			//SalesHistory insert
        			salesHistoryRepository.save(sales);    		
        		}
        	}
    }
    
    
    public ShopDTO getShopByShopNo(int shopNo) {
        return shopRepository.findByShopNo(shopNo);
    }

    public Optional<ShopDTO> optionalFindByMemberNo(int memberNo) {
        return this.shopRepository.optionalFindByMemberNo(memberNo);
    }

    public ShopStatsDTO getShopStats(Integer shopNo) {
        ShopStatsDTO stats = new ShopStatsDTO();

        // 총 주문 건수
        int totalOrders = orderRepository.countByShopNo(shopNo);
        stats.setTotalOrders(totalOrders);

        // 총 매출액
        Integer totalSales = orderRepository.sumOrderPriceByShopNo(shopNo);
        stats.setTotalSales(totalSales != null ? totalSales : 0);

        return stats;
    }
    
    public Map<String, Integer> getMonthlySalesMap(int shopNo) {
        Map<String, Integer> monthlySales = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            int sales = getMonthlySales(shopNo, month);
            // "2025-01" 형식으로 키 지정
            monthlySales.put(String.format("%02d월", month), sales);
        }
        return monthlySales;
    }

    private int getMonthlySales(int shopNo, int month) {
        Integer sum = salesHistoryRepository.sumMonthlySales(shopNo, month);
        return sum != null ? sum : 0;
    }

 // ======================= 정산 신청 =======================
    /**
     * 총 매출액에서 3% 수수료 차감 후 정산 신청
     * @param shopNo 정산할 상점 번호
     * @return 실제 정산 금액
     */
    @Transactional
    public double applySettlement(int shopNo) {
    	//1. 가장 최근 sales_balance 조회
        List<SalesHistoryDTO> salesList = salesHistoryRepository.findByShopNoOrderByCreatedAtDesc(shopNo);
        
        // Status >= 2인 매출만 필터
        double totalSales = salesList.stream()
                //.filter(s -> s.getStatus() >= 2)           // getStatus()는 orderStatus 반환
                .mapToDouble(SalesHistoryDTO::getSalesAmount)
                .sum();

        // 2. 3% 수수료 차감
        double settleAmount = totalSales * 0.97;

        // 3. 정산 내역 DB 저장
        SalesHistoryDTO settlement = new SalesHistoryDTO();
        ShopDTO shop = shopRepository.findByShopNo(shopNo);
        settlement.setShopNo(shop.getShopNo());
        settlement.setSalesAmount((int) settleAmount);  // DTO는 Integer
        settlement.setCreatedAt(LocalDateTime.now());
        settlement.setSalesType(3); 
        //settlement.set// 1: 입금, 2: 환불, 3: 정산

        salesHistoryRepository.save(settlement);

        return settleAmount;
    }    
}
