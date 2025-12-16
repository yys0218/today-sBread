package com.ex.product.model.data;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductDTO { 	//상품 정보 DTO (MyBatis)
	
	private int productNo;				//상품번호
	
	private String productName;			//상품명
	
	private int price;			//상품가격
	
	private int isSubscription;		//정기 결제 여부
	
	private int memberNo;				//판매자 멤버 번호
	
	private int shopNo;					//상점 번호
	
	private String thumbnailName;		//썸네일 명
	
	private String thumbnailPath;		//썸네일 경로
	
	private String categoryMain;		//카테고리 대분류
	
	private String categorySub;			//카테고리 소분류
	
	private String productInfo;			//상세 정보
	
	private String productSummary;		//상품 한 줄 소개
	
	private String allergyInfo;			//알레르기 정보
	
	private String nutritionInfo;		//영양성분
		
	private int status;					//상품 상태
	
	private LocalDateTime createAt;		//등록 날짜

	private LocalDateTime updateAt;		//수정 날짜
	
	private String shopName;	//상점이름
	
	private int reviewCount;	//리뷰 개수
	
	private int shopStatus;		//상점 상태
	
	private int isWished;		//찜 여부 0=안된 1=되어있음
}
