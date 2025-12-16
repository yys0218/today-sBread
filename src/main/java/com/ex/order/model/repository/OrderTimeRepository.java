package com.ex.order.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.data.OrderTimeDTO;

@Repository
public interface OrderTimeRepository extends JpaRepository<OrderTimeDTO, Integer>{
	
	//order객체로 OrderTime 레코드 조회
	Optional<OrderTimeDTO> findByOrder(OrderHistoryDTO order);
	
}
