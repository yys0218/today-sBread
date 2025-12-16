package com.ex.order.model.data;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ScheduleAnnotation { //정기결제에 필요한 결제 예약 정보를 담는 DTO
	
	private final String merchant_uid;	//결제 고유 식별번호 - (필수값) 객체 생성시 set필요
	
	private final long schedule_at;	//결제 예정 시각(초단위) - (필수값) 객체 생성시 set필요
	
	private final int amount;	//결제 예정 금액 - (필수값)
	
	private String name;	//주문명
	
	private String buyer_name;	//주문자 이름
	
	private String buyer_email;	//주문자 메일
	
	private String buyer_tel;	//주문자 전화번호
	
	private String buyer_addr;	//주문자 주소
	
	private String buyer_postcode;	//주문자 우편번호
}
