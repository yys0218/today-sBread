package com.ex.order.model.data;

import java.time.LocalDateTime;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShoppingCartForm { //장바구니에 상품 insert 시 유효성 검사 객체로 사용
	
	//private int cartNo;		//장바구니 번호
	
	@NotNull
	private int productNo;	//상품 번호
	
	@NotNull
	private int price;		//상품 1개당 가격
	
	@NotNull
	@Range(min=1, max=999)
	private int quantity;	//상품 수량
	
	private String productName;	//상품명
	
	private String thumbnailName; //썸네일 파일명
	
	private LocalDateTime cartReg;	//장바구니 담긴 일자
	
	@NotNull(message="로그인 후 구매가 가능합니다.")
	private int memberNo;	//구매자 회원 번호
	
	@NotNull
	private int shopNo;		//가게 번호(판매자/주문 구분용)
	
	  /*
     * Validation 라이브러리
     * 
     * @Size : 문자의 길이제한. (max , min)
     * 
     * @NotNull : Null 허용 X
     * 
     * @NotEmpty : 공백 , Null 허용 X String일 때
     * 
     * @Past : 과거 날짜만 입력 가능
     * 
     * @Future : 미래 날짜만 입력 가능
     * 
     * @FutureOrPresent : 미래 또는 오늘 날짜만 가능
     * 
     * @Max : 최대값 이하의 숫자만 입력 가능
     * 
     * @Min : 최소값 이하의 숫자만 입력 가능
     * 
     * @Pattern : 입력값을 정규식 패턴으로 검증
     */
}
