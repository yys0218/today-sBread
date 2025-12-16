package com.ex.order.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.shop.model.data.ShopDTO;

@Repository
public interface OrderRepository extends JpaRepository<OrderHistoryDTO, Integer> {

	/// 점주별 + 상태 목록	[ 작업자 : 맹재희 ]
    List<OrderHistoryDTO> findByShopShopNoAndStatusInOrderByOrderTimeOrderTimeNoDesc(
            Integer shopNo, List<Integer> statusList);

    // 모든 주문 내역 (점주 기준)	[ 작업자 : 맹재희 ] >>윤예솔	
    List<OrderHistoryDTO> findByShopShopNoAndStatusBetweenOrderByOrderTimeOrderTimeNoDesc(
    	    @Param("shopNo")Integer shopNo, @Param("start")Integer start, @Param("end")Integer end
    	);
    // 상태별 주문 조회	[ 작업자 : 맹재희 ]
    List<OrderHistoryDTO> findByStatusInOrderByOrderTimeOrderTimeNoDesc(List<Integer> statusList);
    
    //	판매자 페이지 배송탭에서 스테이터스 2 인 주문 건만 보이게 하는 쿼리문 [ 작업자 : 맹재희 , 윤예솔]
    @Query("select o from OrderHistoryDTO o where o.shop.shopNo = :shopNo and o.status >= 2  ORDER BY orderTime DESC")
    List<OrderHistoryDTO> findByShopShopNoAndStatus(int shopNo ); //, @Param("status") Integer status 

    // 1️ 총 주문 수	[ 작업자 : 맹재희 ]
    @Query("SELECT COUNT(o) FROM OrderHistoryDTO o WHERE o.shop.shopNo = :shopNo")
    int countByShopNo(@Param("shopNo") Integer shopNo);

    // 2️ 총 매출액 	[ 작업자 : 맹재희 ]
    @Query("SELECT SUM(o.orderPrice) FROM OrderHistoryDTO o WHERE o.shop.shopNo = :shopNo")
    Integer sumOrderPriceByShopNo(@Param("shopNo") Integer shopNo);
    
    //	판매자에겐 모든 주문 내역이 나와야 한다 
    @Query("SELECT od FROM OrderDetailDTO od WHERE od.order.orderNo = :orderNo")
    List<OrderDetailDTO> findOrderDetailsByOrderNo(@Param("orderNo") Integer orderNo);
    
    // 멤버번호로 주문내역에서 조회한 shop의 모든 정보
    @Query("SELECT o.shop.shopNo FROM OrderHistoryDTO o WHERE o.member.memberNo = :memberNo")
    Integer findShopNoByMemberNo(@Param("memberNo") Integer memberNo);




    
	}


