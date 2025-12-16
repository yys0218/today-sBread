package com.ex.shop.model.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * ShopReviewDTO
 * ---------------------------
 * 상점 리뷰 정보를 저장하는 엔티티
 * 각 상점별 리뷰를 관리하며, 평점과 내용을 포함
 */

@Entity
@Table(name="ShopReview")
@Getter
@Setter

public class ShopReviewDTO {
	
	/**
     * 리뷰 번호 (PK)
     * 자동 생성되는 고유 번호
     */
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewNo;					//	리뷰 번호
	
	 /**
     * 상점 번호 (FK)
     * 어떤 상점에 대한 리뷰인지 식별
     */	
    @Column
    private Integer shopNo;						//	상점 번호
    
     /**
     * 리뷰 평점
     * 상점에 대한 평점 (예: 1.0 ~ 5.0)
     */
    @Column
    private Double rating;						//	리뷰 평점
     
    /**
     * 리뷰 내용
     * 사용자가 작성한 리뷰 텍스트
     * 최대 500자
     */
    @Column(length=500)
    private String content;						//	 리뷰 내용

}
