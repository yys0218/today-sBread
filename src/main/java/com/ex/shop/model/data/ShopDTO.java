package com.ex.shop.model.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * ShopDTO
 * ---------------------------
 * 판매자의 빵집 정보를 관리하는 엔티티
 * 상점명, 연락처, 영업 시간, 상태, 입점 신청 정보, 사업자 정보 등 포함
 */

@Entity
@Getter
@Setter
@Table(name = "shop")

// 판매자의 빵집 정보를 관리하는 엔티티 ( 가게명 , 연락처 , 운영 여부 등과 입점 신청 )
public class ShopDTO {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shop_seq")
	@SequenceGenerator(name = "shop_seq", sequenceName = "shop_seq", allocationSize = 1)
	@Column
	private Integer shopNo; // 빵집 번호

	/** 빵집 이름 */
	@Column(length = 100)
	private String shopName;

	/** 신청자 번호 */
	@Column
	private Integer memberNo;

	/** 판매 상태 (판매중:0 , 품절:1) */
	@Column
	private Integer sellStatus;

	/** 빵집 연락처 */
	@Column(length = 50)
	private String shopContact;

	/** 빵집 소개 문구 */
	@Column(length = 500)
	private String shopInfo;

	/** 빵집 전체 주소 */
	@Column(length = 300)
	private String shopAddress;

	/** 빵집 시 / 도 */
	@Column(length = 50)
	private String shopSido;

	/** 빵집 시 / 군 / 구 */
	@Column(length = 50)
	private String shopSigungu;

	/** 빵집 동 / 리 */
	@Column(length = 50)
	private String shopBname;

	/** 빵집 정기 휴무일 */
	@Column(length = 50)
	private String shopDayOff;

	/** 배송비 */
	@Column
	private Integer deliveryFee;

	/** 가게 상태 ( 거절:-1 정상운영:0 , 정지:1 , 폐점:2 ) */
	@Column
	private Integer shopStatus;

	/** 가게 등록일 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private LocalDateTime shopCreatedAt;

	/** 사업자 등록 번호 ( 10 자리 ) */
	@Column(length = 10)
	private String tinNo;

	/** 사업자 이름 */
	@Column(length = 100)
	private String businessName;

	/** 개업 일자 ( YYYY MM DD ) */
	@Temporal(TemporalType.DATE)
	@Column
	private LocalDate businessOpenAt;

	/** 사업자 전화번호 */
	@Column
	private String businessContact;

	/** 사업자 이메일 */
	@Column(length = 100)
	private String businessMail;

	/** 사업자 은행 */
	@Column(length = 50)
	private String businessBank;

	/** 사업자 예금주 이름 */
	@Column(length = 100)
	private String businessAccName;

	/** 사업자 계좌번호 */
	@Column(length = 50)
	private String businessAccNum;

	/** 입점 신청 결과 ( Y / N / C ) */
	@Column(length = 1)
	private String shopRegResult;

	/** 입점 신청 거절 사유(0 : 필수 서류 미비 1 : 자격 요건 불충족 2 : 품질 기준 미달 3 : 중복,허위 기재) */
	@Column
	private Integer shopRegReason;

	/** 입점 신청 날짜 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private LocalDateTime shopRegAt;

	/** 영업 오픈 시간 */
	//@DateTimeFormat(pattern = "a hh:mm")
	private LocalTime openTime;

	/** 영업 마감 시간 */
	//@DateTimeFormat(pattern = "a hh:mm")
	private LocalTime closeTime;

	/**
	 * 폐점 사유 : 0 : 개인 사정(건강, 가족 등)
	 * 1 : 경영 문제(매출부진, 원자재 가격상승)
	 * 2 : 플랫폼 관련(수수료 부담, 주문량 부족)
	 * 3 : 기타
	 */

	@Column
	private int closingReason;

	/** 폐점 사유 상세(기타 사유일때만) */
	@Column
	private String closingReasonDetail;

	/** 폐점 일시 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column
	private LocalDateTime closingAt;

	/** 경도 */
	private Double longitude;

	/** 위도 */
	private Double latitude;
	
	/**현재 시간과 비교한 영업 여부
	 * 0 = 영업 중
	 * 1 = 준비 중
	 * *작업자: 윤예솔
	 */
	@Transient
	private int isOpened;	//장바구니에서 확인용

}
