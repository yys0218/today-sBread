package com.ex.admin.model.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ex.order.model.data.OrderHistoryDTO;

public interface AdminOrderRepository extends JpaRepository<OrderHistoryDTO, Integer> {

    // 기간동안의 총 주문 건수
    @Query("SELECT COUNT(o) FROM OrderHistoryDTO o " +
           "JOIN o.orderTime t " +
           "WHERE o.status = 5 " +
           "AND t.completedAt BETWEEN :start AND :end")
    long countCompletedOrdersToday(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    // 일자별 주문 건수 (주간/기간별 차트용)
    @Query("SELECT FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM-DD'), COUNT(o) " +
           "FROM OrderHistoryDTO o " +
           "JOIN o.orderTime t " +
           "WHERE o.status = 5 " +
           "AND t.completedAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM-DD') " +
           "ORDER BY FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM-DD')")
    List<Object[]> countCompletedOrdersPerDay(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    // 오늘 매출 합계
    @Query("SELECT COALESCE(SUM(o.orderPrice), 0) " +
           "FROM OrderHistoryDTO o " +
           "JOIN o.orderTime t " +
           "WHERE o.status = 5 " +
           "AND t.completedAt BETWEEN :start AND :end")
    long sumCompletedOrderPriceToday(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);

    // 일자별 매출 합계 (주간/기간별 차트용)
    @Query("SELECT FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM-DD'), COALESCE(SUM(o.orderPrice), 0) " +
           "FROM OrderHistoryDTO o " +
           "JOIN o.orderTime t " +
           "WHERE o.status = 5 " +
           "AND t.completedAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM-DD') " +
           "ORDER BY FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM-DD')")
    List<Object[]> sumCompletedOrderPricePerDay(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    // 주별 매출 합계
    @Query("SELECT FUNCTION('TO_CHAR', t.completedAt, 'IYYY-IW'), COALESCE(SUM(o.orderPrice), 0) " +
           "FROM OrderHistoryDTO o " +
           "JOIN o.orderTime t " +
           "WHERE o.status = 5 " +
           "AND t.completedAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('TO_CHAR', t.completedAt, 'IYYY-IW') " +
           "ORDER BY FUNCTION('TO_CHAR', t.completedAt, 'IYYY-IW')")
    List<Object[]> sumCompletedOrderPricePerWeek(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    // 월별 매출 합계
    @Query("SELECT FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM'), COALESCE(SUM(o.orderPrice), 0) " +
           "FROM OrderHistoryDTO o " +
           "JOIN o.orderTime t " +
           "WHERE o.status = 5 " +
           "AND t.completedAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM') " +
           "ORDER BY FUNCTION('TO_CHAR', t.completedAt, 'YYYY-MM')")
    List<Object[]> sumCompletedOrderPricePerMonth(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

}
