package com.ex.product.model.data;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_review") // DB 테이블명
@Getter
@Setter

@Data
public class ProductReviewDTO {
	
	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 자동 생성
	private int reviewNo;				// 후기 고유 번호
	
	private int productNo;				// 상품 고유 번호
	
	private int memberNo;				// 회원 고유 번호
	
	private int orderNo;				// 주문 고유 번호
	
	private String reviewContent;		// 후기 내용
	
	private int rating;					// 별점 (1~5)
	
	private int shopNo; 				// 상점 고유 번호
	
	private LocalDateTime createDate;	// 후기 생성일
	
	private int status;					// 1 : 게시 , 2 : 삭제
	
	// 화면용
	private String memberId;
	private String memberNick;		// 회원 닉네임
	private String productName;
	private String shopName;
	
	
	@Transient
    private List<ProductReviewCommentDTO> comments;

    // getter & setter
    public List<ProductReviewCommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<ProductReviewCommentDTO> comments) {
        this.comments = comments;
    }
		
	
}