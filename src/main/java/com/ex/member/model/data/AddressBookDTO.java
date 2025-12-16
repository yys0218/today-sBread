package com.ex.member.model.data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ADDRESS_BOOK")
public class AddressBookDTO {
	// 기본키
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_seq")
	@SequenceGenerator(name = "address_seq", sequenceName = "ADDRESS_SEQ", allocationSize = 1)
	private int addressNo; // 배송지 번호

	// 회원 번호 (FK)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MEMBER_NO", nullable = false)
	private MemberDTO member; // 연관된 회원

	// 배송지 상세 주소
	@Column(length = 300, nullable = false)
	private String addressDetail;

	// 수령인 이름
	@Column(length = 50, nullable = false)
	private String receiverName;

	// 수령인 연락처
	@Column(length = 20, nullable = false)
	private String receiverPhone;

	// 기본 배송지 여부 (Y/N)
	@Column(nullable = false)
	private String isDefault = "Y";

	// 등록일
	private LocalDateTime createdAt;
	
	// 경도 X longitude
	private Double longitude;
	// 위도 Y latitude
	private Double latitude;
}