package com.ex.center.model.data;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistryForm {

	/** 사업자 등록 번호 ( 10 자리 ) */
	@NotNull
	private String tinNo;

	/** 사업자 이름 */
	@NotNull
	private String businessName;

	/** 개업 일자 ( YYYY MM DD ) */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull
	private LocalDate businessOpenAt;

	/** 사업자 전화번호 */
	@NotNull
	private String businessContact;

	/** 사업자 이메일 */
	@NotNull
	private String businessMail;

	/** 사업자 은행 */
	@NotNull
	private String businessBank;

	/** 사업자 예금주 이름 */
	@NotNull
	private String businessAccName;

	/** 사업자 계좌번호 */
	@NotNull
	private String businessAccNum;

	/** 가게 이름 */
	private String shopName;

	/** 도로명 주소 */
	private String shopRoadAdd;

	/** 상세 주소 */
	private String shopDetailAdd;

	/** 연락처 */
	private String shopContact;

	/** 영업 오픈시간 */
	@DateTimeFormat(pattern = "a hh:mm")   
	private LocalTime openTime;

	/** 영업 마감시간 */
	@DateTimeFormat(pattern = "a hh:mm")   
	private LocalTime closeTime;

	/** 소개 문구 */
	private String shopInfo;

	/** 시 / 도 */
	private String shopSido;

	/** 시 / 군 / 구 */
	private String shopSigungu;

	/** 동 / 리 */
	private String shopBname;

	/** 정기 휴무일 */
	private String shopDayOff;

	/** 배송비 */
	private Integer deliveryFee;

}