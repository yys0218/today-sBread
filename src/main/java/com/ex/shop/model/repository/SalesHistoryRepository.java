package com.ex.shop.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ex.shop.model.data.SalesHistoryDTO;
import com.ex.shop.model.data.ShopDTO;

/**
 * SalesHistoryRepository
 * --------------------------------------------------------------------------------------------------------
 * 매출 내역(SalesHistory) 관련 JPA Repository
 * - 상점 기준 매출 통계 조회
 * - 상점 기준 매출 목록 조회
 * - 최근 매출 조회
 */
public interface SalesHistoryRepository extends JpaRepository<SalesHistoryDTO, Integer> {

    // ======================= 통계 관련 =======================

    /**
     * 상점(shopNo) 기준 주문 건수 조회
     * - salesType = 1(주문)만 포함
     *
     * @param shopNo 상점 번호
     * @return int 주문 건수
     */
    @Query("SELECT COUNT(s) FROM SalesHistoryDTO s WHERE s.shopNo = :shopNo AND s.salesType = 1")
    int countByShopNo(@Param("shopNo") int shopNo);

    /**
     * 상점(shopNo) 기준 총 매출 합계 조회
     * - salesType = 1(주문)만 포함
     * - 값이 없으면 0 반환
     *
     * @param shopNo 상점 번호
     * @return int 총 매출
     */
    @Query("SELECT COALESCE(SUM(s.salesAmount),0) FROM SalesHistoryDTO s WHERE s.shopNo = :shopNo AND s.salesType = 1")
    int totalSalesByShopNo(@Param("shopNo") int shopNo);

    // ======================= 조회 관련 =======================

    /**
     * 상점(shopNo) 기준 매출 내역 조회 (기본 JPA 방식)
     *
     * @param shopNo 상점 번호
     * @return List<SalesHistoryDTO> 매출 내역
     */
    List<SalesHistoryDTO> findByShopNo(Integer shopNo);

    /**
     * 상점(shopNo) 기준 매출 내역 조회 (Native Query, 최신순)
     *
     * @param shopNo 상점 번호
     * @return List<SalesHistoryDTO> 매출 내역
     */
    @Query(value = "SELECT * FROM SalesHistoryDTO WHERE shop_no = :shopNo ORDER BY createdAt DESC", nativeQuery = true)
    List<SalesHistoryDTO> findByShopNoNative(@Param("shopNo") Integer shopNo);
    
    /**
     * 작업자: 윤예솔
     * 주문번호에 해당하는 매출내역 
     * @param orderNo
     * @return Optional<SalesHistoryDTO>
     */
    Optional<SalesHistoryDTO> findByOrderNo(int orderNo);
    
    /**
     * 가장 최근 잔액
     * @param shop 상점 객체
     * @return int balance 상점 매출 잔액
     */
    @Query("select s.salesBalance from SalesHistoryDTO s where s.shopNo =:shopNo order by s.createdAt desc")
    Optional<SalesHistoryDTO> findTop1ByshopNoOrderByCreatedAtDesc(int shopNo);
    
    /**
     * 상점(shopNo) 기준 매출 내역 조회 (최신순 정렬)
     *
     * @param shopNo 상점 번호
     * @return List<SalesHistoryDTO> 매출 내역
     */
    List<SalesHistoryDTO> findByShopNoOrderByCreatedAtDesc(Integer shopNo);

    /**
     * 상점(shopNo) 기준 가장 최근 매출 단건 조회
     * - 정산 시 사용
     *
     * @param shopNo 상점 번호
     * @return Optional<SalesHistoryDTO> 최근 매출
     */
    Optional<SalesHistoryDTO> findTopByShopNoOrderBySalesNoDesc(int shopNo);

    @Query(value = "SELECT * FROM (SELECT * FROM sales_history WHERE shop_no = :shopNo ORDER BY created_at DESC) WHERE ROWNUM = 1", nativeQuery = true)
    Optional<SalesHistoryDTO> findTopByShopNoOrderByCreatedAtDesc(@Param("shopNo") int shopNo);

    @Modifying
    @Query(value = "INSERT INTO sales_history ( sales_no, order_no, shop_no, sales_type, sales_amount,  sales_balance, created_at,  order_status,  product_name, quantity ) VALUES ( sales_history_seq.NEXTVAL, 0, :shopNo, 3, :settleAmount, 0, SYSDATE,  3, '정산', 1 )", nativeQuery = true)
    void newSh(@Param("shopNo") int shopNo, @Param("settleAmount") int settleAmount);

    /**
     * 주문 상태(status) 목록 기준 매출 조회
     *
     * @param statuses 주문 상태 코드 목록
     * @return List<SalesHistoryDTO> 매출 내역
     */

    //List<SalesHistoryDTO> findByOrderStatusIn(List<Integer> statuses); 
    
    // 월별 매출 합계
    @Query("SELECT SUM(s.salesAmount) FROM SalesHistoryDTO s " +
    	       "WHERE s.shopNo = :shopNo AND EXTRACT(MONTH FROM s.createdAt) = :month")
    	Integer sumMonthlySales(@Param("shopNo") int shopNo, @Param("month") int month);
    
    //	정산 내역 보는 쿼리문
    @Query("SELECT s FROM SalesHistoryDTO s WHERE s.shopNo = :shopNo AND s.salesType = 3 ORDER BY s.createdAt DESC")
    List<SalesHistoryDTO> findSettlementHistory(@Param("shopNo") int shopNo);

    List<SalesHistoryDTO> findByOrderStatusIn(List<Integer> statuses);

    // 월별 매출 합계
   // @Query("SELECT SUM(s.salesAmount) FROM SalesHistory s " +
   //         "WHERE s.shopNo = :shopNo AND EXTRACT(MONTH FROM s.createdAt) = :month")
   // Integer sumMonthlySales(@Param("shopNo") int shopNo, @Param("month") int month);




}
