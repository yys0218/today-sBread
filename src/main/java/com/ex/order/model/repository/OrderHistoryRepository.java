package com.ex.order.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.MemberDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.shop.model.data.ShopDTO;

@Repository // 주문 내역 레퍼지토리
public interface OrderHistoryRepository extends JpaRepository<OrderHistoryDTO, Integer> {
	//작업자 이름 써주세요~!~!!
	
	// 작업자 : 안성진
	// 마이페이지 - 주문 내역
	// 주문자 (Member) 기준으로 주문 내역 가져오기 + 주문번호 내림차순 정렬(예솔 추가)
	List<OrderHistoryDTO> findByMemberOrderByOrderNoDesc(MemberDTO member);
	
	// 작업자 : 안성진
	// 마이페이지 - 주문 내역 - 주문 상세 페이지
	@Query("select oh from OrderHistoryDTO oh left join fetch oh.shop left join fetch oh.orderTime where oh.orderNo = :orderNo and oh.member.memberNo = :memberNo")
	Optional<OrderHistoryDTO> findDetailForMember(@Param("orderNo") int orderNo, @Param("memberNo") int memberNo);
	
	// 작업자 : 안성진
	// 마이페이지 - 정기 결제 조회/해지
	// 정기결제 목록 : customerUid(빌링키)가 있는 것이 구독
	// 마이페이지 - 정기 결제 조회/해지 (페이징 지원)
	@Query(value = "select oh from OrderHistoryDTO oh  join fetch oh.shop  where oh.member = :member and oh.customerUid is not null",
	       countQuery = "select count(oh) from OrderHistoryDTO oh  where oh.member = :member and oh.customerUid is not null")
	Page<OrderHistoryDTO> findSubscriptionByMember(@Param("member") MemberDTO member, Pageable pageable);

	
	// 작업자 : 안성진
	// 본인 소유 + orderNo로 단건 조회 (마이페이지에 안전한 접근을 위해 사용)
	Optional<OrderHistoryDTO> findByOrderNoAndMemberMemberNo(int orderNo, int memberNo);
	
	// 작업자 : 안성진
	// 마이페이지 - 주문 내역 - 페이지네이션
	Page<OrderHistoryDTO> findByMember(MemberDTO member, Pageable pageable);

	
	// 라이더
	// StatusGreaterThanEqual : status >= ? 조건
	// 최신 10개 주문 가져오기
	List<OrderHistoryDTO> findTop10ByStatusGreaterThanEqualOrderByOrderNoDesc(int status);

	// 작업자: 윤예솔
	// 주문 - 결제완료된 주문 CRUD
	// 주문자 (Member) 기준 주문 내역 중 가장 최근 주문 번호 가져오기
	@Query("select max(o.orderNo) from OrderHistoryDTO o where o.member = :member order By orderNo desc")
	Optional<Integer> getOrderNoByMember(@Param("member") MemberDTO member);
	
	// 작업자: 윤예솔
	// 결제일이 오늘인 레코드 가져오기
	@Query("select o from OrderHistoryDTO o where FUNCTION('trunc', FUNCTION('TO CHAR', o.nextPaymentAt)) = FUNCTION('trunc', FUNCTION('TO CHAR', FUNCTION('sysdate')))")
	List<OrderHistoryDTO> findByNextPaymentAt();
	
	// 작업자: 윤예솔
	// 배송일이 오늘인 레코드 가져오기
	@Query("select o from OrderHistoryDTO o where FUNCTION('trunc', FUNCTION('TO CHAR', o.deliveryCycle)) = FUNCTION('trunc', FUNCTION('TO CHAR', FUNCTION('sysdate')))")
	List<OrderHistoryDTO> findByDeliveryCycle();

	//작업자: 윤예솔
	//상태가 n인 주문내역 레코드 가져오기
	//@Query("select o from OrderHistoryDTO o where Shop")
	List<OrderHistoryDTO> findByShopAndStatus(ShopDTO shop, int status);
	
	// orderNo 기준 내림차순으로 작은 값부터 10개 가져오기
	List<OrderHistoryDTO> findTop10ByStatusAndOrderNoLessThanOrderByOrderNoDesc(int status, int orderNo);
	
	/*
	 * @Query("SELECT o FROM OrderHistoryDTO o " +
	 * "WHERE o.status >= :status " +
	 * "AND o.orderNo < :orderNo " +
	 * "ORDER BY o.orderNo DESC")
	 * List<OrderHistoryDTO> findMoreScroll(@Param("status") int status,
	 * 
	 * @Param("orderNo") int orderNo);
	 */
	List<OrderHistoryDTO> findTop10ByStatusGreaterThanEqualAndOrderNoLessThanOrderByOrderNoDesc(
			int status, int orderNo);

	// orderNo를 기준으로 orderHistory를 조회
	Optional<OrderHistoryDTO> findByOrderNo(int orderNo);

	/**
	 * 
	 * @param status 상태값 (2:판매자 주문 수락 , 3:라이더 배송 수락 , 4:픽업/배송시작 , 5:배송완료 )
	 * @return List<OrderHistoryDTO> 조건에 맞는 최근 10건의 주문 내역 리스트
	 */
	List<OrderHistoryDTO> findTop10ByStatusOrderByOrderNoDesc(int status);

	/**
	 * 
	 * @param status 상태값 (2:판매자 주문 수락 , 3:라이더 배송 수락 , 4:픽업/배송시작 , 5:배송완료 ),
	 * @param member 조회할 라이더(Member 엔티티)
	 * @return List<OrderHistoryDTO> 조건에 맞는 최근 10건의 주문 내역 리스트
	 */
	List<OrderHistoryDTO> findTop10ByStatusAndRiderOrderByOrderNoDesc(int status, MemberDTO member);

	  OrderHistoryDTO findByMerchantUid(String merchantUid);

	  static List<OrderHistoryDTO> findByShop_ShopNo(Integer shopNo) {
		
		return null;
	  }
	  
	// 판매자(shopNo) 기준 전체 주문 내역 조회
	  static List<OrderHistoryDTO> findByShop_ShopNoOrderByOrderNoDesc(Integer shopNo) {
		// TODO Auto-generated method stub
		return null;
	}

	  // 판매자(shopNo) 기준 + 상태(status) 조회
	  List<OrderHistoryDTO> findByShop_ShopNoAndStatus(Integer shopNo, int status);

	
}
