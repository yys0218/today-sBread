package com.ex.order.model.data;

import java.time.LocalDateTime;

import com.ex.product.model.data.ProductDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "order_Detail")
public class OrderDetailDTO { // 주문 상세 상품 내역

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_detail_seq")
	@SequenceGenerator(name = "order_detail_seq", sequenceName = "order_detail_seq", allocationSize = 1)
	private int orderDetailNo; // 주문상세 식별번호

	@ManyToOne() // 주문 상세 N개는 1개의 주문 내역을 가질 수 있다.
	// @Column(nullable = false)
	@JsonIgnore
	// @JsonBackReference // 역참조 직렬화 방지
	@JsonBackReference
	private OrderHistoryDTO order; // 주문

	// @ManyToOne
	@Column(nullable = false)
	private int productNo; // 상품 번호

	@Column(nullable = false)
	private int quantity; // 수량

	@Column(nullable = false)
	private int price; // 1개당 가격

	@Column(nullable = true)
	private LocalDateTime deliveryCycle; // 배송 주기 - 정기 배송상품일시 입력됨

	private LocalDateTime detailReg; // 상세 입력 일시

	@Transient
	private ProductDTO product;

}
