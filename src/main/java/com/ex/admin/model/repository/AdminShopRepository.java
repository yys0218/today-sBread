package com.ex.admin.model.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ex.shop.model.data.ShopDTO;

public interface AdminShopRepository extends JpaRepository<ShopDTO, Integer> {

        @Query(value = "SELECT s FROM ShopDTO s " +
                     "WHERE (:searchKeyword IS NULL OR s.businessName LIKE %:searchKeyword% OR s.shopName LIKE %:searchKeyword%) "
                     +
                     "AND (:roleValue IS NULL OR s.shopStatus = :roleValue)" +
                        "and s.shopRegResult = 'Y'", countQuery = "select count(s) from ShopDTO s " +
                                        "where (:searchKeyword IS NULL OR s.businessName LIKE %:searchKeyword% OR s.shopName LIKE %:searchKeyword%) " +
                                        "and s.shopRegResult = 'Y'")
        Page<ShopDTO> searchShopList(@Param("roleValue") Integer roleValue,
                     @Param("searchKeyword") String searchKeyword, Pageable pageable);

        @Query("select s from ShopDTO s where (:result is null or :result = '' or s.shopRegResult = :result) ")
        Page<ShopDTO> adminRegistryList(@Param("result") String result, Pageable pageable);

        @Query("select s from ShopDTO s where s.shopStatus = 2 and s.closingAt >= :startDate order by s.closingAt desc")
        Page<ShopDTO> adminClosingList(@Param("startDate") LocalDateTime startDate, Pageable pageable);

        Page<ShopDTO> findByShopRegResult(String shopRegResult, Pageable pageable);

        Page<ShopDTO> findByShopRegResultIsNull(Pageable pageable);

        ShopDTO findByMemberNo(int memberNo);

        @Query("select s.memberNo from ShopDTO s where s.shopNo = :shopNo")
        int findMemberNoByShopNo(@Param("shopNo") int shopNo);

        @Query("select s.shopStatus from ShopDTO s where s.memberNo = :memberNo")
        int findTopShopStatusByMemberNo(@Param("memberNo") int memberNo);

        @Query("SELECT s FROM ShopDTO s WHERE s.shopStatus = 2")
        Page<ShopDTO> findByShopStatus(Pageable pageable);

        Optional<ShopDTO> findByShopName(String shopName);

        Optional<ShopDTO> findTopByShopNameAndShopRegResult(String shopName, String ShopResResult);

        Optional<ShopDTO> findTopByShopContactAndShopRegResult(String ShopContact, String ShopRegResult);

        // 차트
        // 기간별 신규 입점 신청 건수
        @Query("SELECT COUNT(s) FROM ShopDTO s " +
                        "WHERE s.shopCreatedAt BETWEEN :start AND :end")
        long countNewShopsBetween(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 일자별 신규 입점 신청 건수
        @Query("SELECT FUNCTION('TO_CHAR', s.shopCreatedAt, 'YYYY-MM-DD'), COUNT(s) " +
                        "FROM ShopDTO s " +
                        "WHERE s.shopCreatedAt BETWEEN :start AND :end " +
                        "GROUP BY FUNCTION('TO_CHAR', s.shopCreatedAt, 'YYYY-MM-DD') " +
                        "ORDER BY FUNCTION('TO_CHAR', s.shopCreatedAt, 'YYYY-MM-DD')")
        List<Object[]> countNewShopsPerDay(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 이번 주 폐점 수
        @Query("SELECT COUNT(s) FROM ShopDTO s " +
                        "WHERE s.shopStatus = 2 " +
                        "AND s.closingAt BETWEEN :start AND :end")
        long countClosedShopsBetween(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        // 일자별 폐점 수
        @Query("SELECT FUNCTION('TO_CHAR', s.closingAt, 'YYYY-MM-DD'), COUNT(s) " +
                        "FROM ShopDTO s " +
                        "WHERE s.shopStatus = 2 " +
                        "AND s.closingAt BETWEEN :start AND :end " +
                        "GROUP BY FUNCTION('TO_CHAR', s.closingAt, 'YYYY-MM-DD') " +
                        "ORDER BY FUNCTION('TO_CHAR', s.closingAt, 'YYYY-MM-DD')")
        List<Object[]> countClosedShopsPerDay(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

}
