package com.ex.order.model.data;

import java.time.LocalDateTime;

import com.ex.member.model.data.MemberDTO;
import com.ex.shop.model.data.ShopDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "shoppingCart")
public class ShoppingCartDTO { // 장바구니 내역

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_cart_seq")
	@SequenceGenerator(name = "shopping_cart_seq", sequenceName = "shopping_cart_seq", allocationSize = 1)
	private int cartNo; // 장바구니 번호

	// @ManyToOne //n개의 장바구니는 1개의 상품을 가질 수 있다.
	@Column(nullable = false)
	private int productNo; // 상품 번호
	
	//@Column(nullable = false)
	private String productName; // 상품명

	@Column(nullable = false)
	private int quantity; // 상품 수량
	
	@Column(nullable = false)
	private int price; // 1개당 가격
	
	private String ThumbnailName; //썸네일명

	private LocalDateTime cartReg; // 장바구니 담긴 일자

	@ManyToOne // n개의 장바구니는 1개의 회원을 가질 수 있다.
	// @Column(nullable = false)
	private MemberDTO member; // 구매자 회원 정보

	@ManyToOne // n개의 장바구니는 1개의 상점을 가질 수 있다.
	// @Column(nullable = false)
	private ShopDTO shop; // 상점 정보(판매자/주문 구분용)
	
	@Transient
	private int status; //판매 상태 0=판매 중 1=품절 2=슴김
	
	@Transient
	private int isWished; //찜 여부
}
