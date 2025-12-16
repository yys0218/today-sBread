package com.ex.order.model.data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "order_Time")
public class OrderTimeDTO { // 주문 발생, 상태 변경 일시 내역

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_time_seq")
	@SequenceGenerator(name = "order_time_seq", sequenceName = "order_time_seq", allocationSize = 1)
	private int orderTimeNo; // 주문 시간 식별번호

	@OneToOne // 주문 시간은 1개의 주문 내역을 가진다.
	// @JoinColumn(name = "order_no") // 참조할 DB내의 컬럼명
	@JsonIgnore
	private OrderHistoryDTO order; // 주문 내역

	private LocalDateTime orderedAt; // 고객 주문 시간

	private LocalDateTime rejectedAt; // 판매자 주문 거절 시간

	private LocalDateTime requestedAt; // 판매자 주문 수락&배송 요청 시간

	private LocalDateTime assignedAt; // 라이더 배송 수락 시간

	private LocalDateTime pickupAt; // 라이더 픽업 완료 시간

	private LocalDateTime completedAt; // 라이더 배송 완료 시간

	private LocalDateTime canceledAt; // 고객 주문 취소 시간

	@Column(length = 200)
	private String cancelReason; // 취소 사유

	private LocalDateTime estPickupAt; // 픽업 예정 시간

	private LocalDateTime estDeliveryAt; // 배송완료 예정 시간

	// 연관관계 편의 메서드
	/*
	 * public void setOrderHistory(OrderHistoryDTO orderHistory) {
	 * this.orderHistory = orderHistory;
	 * if(orderHistory.getOrderTime() != this) {
	 * orderHistory.setOrderTime(this);
	 * }
	 * }
	 */

}
