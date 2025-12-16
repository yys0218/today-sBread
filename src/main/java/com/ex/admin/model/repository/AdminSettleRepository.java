package com.ex.admin.model.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ex.admin.model.data.SettleDTO;

public interface AdminSettleRepository extends JpaRepository<SettleDTO, Integer> {

        Page<SettleDTO> findAll(Pageable pageable);

        Page<SettleDTO> findBySettleNameContainingIgnoreCase(String keyword, Pageable pageable);

        List<SettleDTO> findBySettleNameContainingIgnoreCase(String keyword, Sort sort);

        // 일간
    @Query("SELECT TO_CHAR(s.settleAt, 'YYYY-MM-DD'), SUM(s.settleCharge) " +
            "FROM SettleDTO s " +
            "WHERE s.settleAt BETWEEN :startDate AND :endDate " +
            "AND (:memberNo IS NULL OR s.settleRef = :memberNo) " +
            "AND (:roleFilter IS NULL OR s.settleType = :roleFilter) " +
            "GROUP BY TO_CHAR(s.settleAt, 'YYYY-MM-DD') " +
            "ORDER BY TO_CHAR(s.settleAt, 'YYYY-MM-DD')")
    List<Object[]> findDailyCharge(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("memberNo") Integer memberNo,
            @Param("roleFilter") Integer roleFilter);

        // 주간
    @Query("SELECT TO_CHAR(s.settleAt, 'IYYY-IW'), SUM(s.settleCharge) " +
            "FROM SettleDTO s " +
            "WHERE s.settleAt BETWEEN :startDate AND :endDate " +
            "AND (:memberNo IS NULL OR s.settleRef = :memberNo) " +
            "AND (:roleFilter IS NULL OR s.settleType = :roleFilter) " +
            "GROUP BY TO_CHAR(s.settleAt, 'IYYY-IW') " +
            "ORDER BY TO_CHAR(s.settleAt, 'IYYY-IW')")
    List<Object[]> findWeeklyCharge(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("memberNo") Integer memberNo,
            @Param("roleFilter") Integer roleFilter);

        // 월간
        @Query("SELECT TO_CHAR(s.settleAt, 'YYYY-MM'), SUM(s.settleCharge) " +
            "FROM SettleDTO s " +
            "WHERE s.settleAt BETWEEN :startDate AND :endDate " +
            "AND (:memberNo IS NULL OR s.settleRef = :memberNo) " +
            "AND (:roleFilter IS NULL OR s.settleType = :roleFilter) " +
            "GROUP BY TO_CHAR(s.settleAt, 'YYYY-MM') " +
            "ORDER BY TO_CHAR(s.settleAt, 'YYYY-MM')")
        List<Object[]> findMonthlyCharge(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("memberNo") Integer memberNo,
            @Param("roleFilter") Integer roleFilter);

        // 정산금액 (settleAmt) 조회
        @Query("SELECT TO_CHAR(s.settleAt, 'YYYY-MM-DD'), SUM(s.settleAmt) " +
                        "FROM SettleDTO s " +
                        "WHERE s.settleAt BETWEEN :startDate AND :endDate " +
                        "AND (:memberNo IS NULL OR s.settleRef = :memberNo) " +
                        "AND (:roleFilter IS NULL OR s.settleType = :roleFilter) " +
                        "GROUP BY TO_CHAR(s.settleAt, 'YYYY-MM-DD') " +
                        "ORDER BY TO_CHAR(s.settleAt, 'YYYY-MM-DD')")
        List<Object[]> findDailyAmt(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("memberNo") Integer memberNo,
                        @Param("roleFilter") Integer roleFilter);

        @Query("SELECT TO_CHAR(s.settleAt, 'IYYY-IW'), SUM(s.settleAmt) " +
                        "FROM SettleDTO s " +
                        "WHERE s.settleAt BETWEEN :startDate AND :endDate " +
                        "AND (:memberNo IS NULL OR s.settleRef = :memberNo) " +
                        "AND (:roleFilter IS NULL OR s.settleType = :roleFilter) " +
                        "GROUP BY TO_CHAR(s.settleAt, 'IYYY-IW') " +
                        "ORDER BY TO_CHAR(s.settleAt, 'IYYY-IW')")
        List<Object[]> findWeeklyAmt(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("memberNo") Integer memberNo,
                        @Param("roleFilter") Integer roleFilter);

        @Query("SELECT TO_CHAR(s.settleAt, 'YYYY-MM'), SUM(s.settleAmt) " +
                        "FROM SettleDTO s " +
                        "WHERE s.settleAt BETWEEN :startDate AND :endDate " +
                        "AND (:memberNo IS NULL OR s.settleRef = :memberNo) " +
                        "AND (:roleFilter IS NULL OR s.settleType = :roleFilter) " +
                        "GROUP BY TO_CHAR(s.settleAt, 'YYYY-MM') " +
                        "ORDER BY TO_CHAR(s.settleAt, 'YYYY-MM')")
        List<Object[]> findMonthlyAmt(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("memberNo") Integer memberNo,
                        @Param("roleFilter") Integer roleFilter);
}
