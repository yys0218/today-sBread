package com.ex.order.model.data;

import java.time.LocalDateTime;
import java.util.List;

import com.ex.member.model.data.MemberDTO;
import com.ex.product.model.data.ProductDTO;
import com.ex.shop.model.data.ShopDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "order_History")
public class OrderHistoryDTO { // 주문 내역

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_history_seq")
	@SequenceGenerator(name = "order_history_seq", sequenceName = "order_history_seq", allocationSize = 1)
	private int orderNo; // 주문 기록 식별번호

	// @JoinColumn(name = "SHOP_SHOP_NO", nullable=false)
	@ManyToOne // 가게는 여러 주문을, 주문은 하나의 가게만 가질 수 있다.
	// @Column(nullable = false)
	private ShopDTO shop; // 가게 테이블

	// @JoinColumn(name = "MEMBER_MEMBER_NO", nullable=false)
	@ManyToOne // 멤버는 여러 주문을, 주문은 하나의 멤버만 가질 수 있다.
	// @Column(nullable = false)
	@JsonIgnore
	private MemberDTO member; // 구매자 번호

	// @OneToMany(cascade = CascadeType.ALL) // 주문내역은 N개의 주문상세를 가질 수 있음 (레코드 삭제 시
	// 연결된 OrderDetail도 삭제되도록)
	@OneToMany(mappedBy = "orderDetailNo", cascade = CascadeType.ALL)
	@JsonManagedReference // 부모
	private List<OrderDetailDTO> orderDetail; // 주문 상세

	@Column(length = 4000, nullable = false)
	private String orderAddress; // 배송지

	@Column(length = 4000, nullable = false)
	private String orderName; // 받는 사람 이름

	@Column(length = 4000, nullable = false)
	private String orderPhone; // 받는 사람 번호

	@Column(length = 4000)
	private String orderRequest; // 요청사항

	@Column(unique = true, nullable = false)
	private String merchantUid; // 결제된 주문 식별 번호

	@Column(nullable = false)
	private int deliveryFee; // 배송비

	@Column(nullable = false)
	private int orderPrice; // 총 결제 금액

	@Column(columnDefinition = "Number default 0")
	private int status; // 주문 진행 상태

	@ManyToOne // 주문은 하나의 라이더를, 라이더는 여러 주문을 가짐
	@JsonIgnore
	private MemberDTO rider; // 배달기사 Member객체

	// @JoinColumn(name = "ORDER_TIME_ORDER_TIME_NO")
	@OneToOne(cascade = CascadeType.ALL) // 주문내역은 1개의 주문시간을 가짐 (OrderHistory 저장 시 OrderTime 저장되도록)
	// @Column(nullable = false)
	private OrderTimeDTO orderTime;

	@Column(length = 4000)
	private String filepath; // 배송완료 사진 (주소+파일명)

	@Transient // DB에 컬럼 생성 없이 사용
	private Double distance;

	@Transient // DB에 컬럼 생성 없이 사용
	private Double time;

	private LocalDateTime deliveryCycle; // 배송주기

	private String customerUid; // 빌링키 (정기 결제 시 필요한, 결제수단마다 달라져야하는 고유한 번호)

	private LocalDateTime nextPaymentAt; // 다음 결제일

	@Column(length = 100)
	private String alert; // 정기결제 전 알림 여부

	@Transient // 해당 주문의 대표 product (주문내역 출력용)
	private ProductDTO product;

	@Transient /** 해당 주문의 이름(정기 결제용) */
	private String name;
}
