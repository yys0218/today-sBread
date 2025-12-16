package com.ex.order.model.data;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.ex.shop.model.data.ShopDTO;

import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderForm { //주문 페이지 유효성 검사& 폼 파라미터 전달용
	//private int orderNo; // 주문 기록 식별번호 테이블 insert 시 생성됨
	
	private ArrayList<Integer> cartNos; //결제된 장바구니 번호들
	
	private int totalPrice;	// 상품 총액
	
	@NotNull
	private int orderPrice; // 총 결제 금액

	@NotNull
	private int buyerNo; // 구매자 번호
	
	@NotNull
	private int shopNo; // 가게 테이블 form의 shopNo가 매핑됨?
	
	@NotNull
	private int deliveryFee; // 배송비

	@NotNull(message="받을 주소를 입력해주세요.")
	private String orderAddress; // 배송지

	@NotNull(message="받는 사람을 입력해주세요.")
	private String orderName;	//받는 사람 이름
	
	@NotNull(message="받는 사람 연락처를 입력해주세요.")
	private String orderPhone;		//받는 사람 번호
	

	private String orderRequest; // 요청사항

	@NotNull
	private String merchantUid; // 결제된 주문 식별 번호

	//private int status; // 주문 진행 상태 테이블 insert 시 생성됨
	
	//@OneToOne(mappedBy = "orderTime", cascade = CascadeType.REMOVE ) //orderTime 주문시간 테이블 참조
	//private OrderTimeDTO orderTime; // 주문 시간 테이블
	
	private int deliveryCycle; // 배송주기 >> DB 입력 시 LocalDateTime객체로 입력

	private String customerUid; //결제수단 식별 번호
	
	private String name; //주문명: '테스트결제'로 일괄 입력됨

	//	@DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	//private LocalDateTime nextPaymentAt; // 다음 결제일 (결제일 + 31일)
	
	private String alert; // 정기결제 전 알림 여부

	//@FutureOrPresent //미래 또는 오늘 날짜만 가능
	//private LocalDateTime date;	//두 번째 배송일로 입력받은 날짜
	
	//private int riderNo;	//배달기사 참조번호
	
	//@Column(length = 4000)
	//private String filepath;	//배송완료 사진 (주소+파일명)
}
