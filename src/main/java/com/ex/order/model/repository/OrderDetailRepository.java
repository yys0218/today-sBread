package com.ex.order.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderHistoryDTO;

@Repository // 주문 상세 내역 레퍼지토리
public interface OrderDetailRepository extends JpaRepository<OrderDetailDTO, Integer> {
	//작업자 이름 써주세요~!~!!
	
	//작업자 : 예솔..?
    @Query("SELECT od FROM OrderDetailDTO od WHERE od.order.orderNo = :orderNo")
    List<OrderDetailDTO> findByOrderNo(@Param("orderNo") int orderNo);


    // 작업자 : 안성진
    // 주문 객체로 주문 상세내역 리스트 조회
    Optional<List<OrderDetailDTO>> findByOrder(OrderHistoryDTO order);
    
}
