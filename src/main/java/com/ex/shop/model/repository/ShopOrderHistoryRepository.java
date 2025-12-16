package com.ex.shop.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.shop.model.data.ShopDTO;

/**
 * ShopOrderHistoryRepository
 * --------------------------------------------------------------------------------------------------------
 * 판매자 상점 기준 주문내역(OrderHistory) 조회를 위한 JPA Repository
 * - 상점 기준 주문내역 조회
 */
@Repository
public interface ShopOrderHistoryRepository extends JpaRepository<OrderHistoryDTO, Integer> {

    // ======================= 상점 기준 주문 조회 =======================

    /**
     * 특정 상점(shop) 기준 주문 내역 조회
     *
     * @param shop 조회할 상점 엔티티
     * @return List<OrderHistoryDTO> 해당 상점의 주문 내역
     */
    List<OrderHistoryDTO> findByShop(ShopDTO shop);
}
