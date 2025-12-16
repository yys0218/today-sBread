package com.ex.shop.model.data;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data

/**
 * ShopStatsDTO
 * ---------------------------
 * 판매자 상점 통계 정보를 담는 DTO
 * 단순 조회용으로 JPA Repository 필요 없음
 * 테이블 정의서도 존재하지 않음
 */

public class ShopStatsDTO {

    /** 기본 키, 자동 생성 */
    // 각 통계 레코드를 고유하게 식별하는 ID
    @Transient
    private Integer statsId;

    /** 상점 번호 (Shop.shopNo)와 연관 */
    @Transient
    private Integer shopNo;

    /** 총 주문 건수 */
    @Transient
    private int totalOrders;

    /** 리뷰 수 */
    @Transient
    private int totalReviews;

    /** 리뷰 평균 평점 (0.0 ~ 5.0) */
    @Transient
    private double avgRating;

    /** 총 매출액 (원 단위) */
    @Transient
    private int totalSales;
    
    /** 월간 통계: key = "YYYY-MM" 형식, value = 주문 건수 */
    @Transient
    private Map<String, Integer> monthlyOrders = new HashMap<>();

    /** 월간 통계: key = "YYYY-MM" 형식, value = 매출액 */
    @Transient
    private Map<String, Integer> monthlySales = new HashMap<>();

}
