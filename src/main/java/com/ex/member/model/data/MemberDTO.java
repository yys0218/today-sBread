package com.ex.member.model.data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.ex.rider.model.data.RiderCommunityDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

/*	MEMBER 테이블과 매핑되는 엔티티
	PK : MEMBER_NO (오라클 시퀀스 MEMBER_SEQ 사용)
	아이디/이메일/전화번호는 유니크
	가입일은 생성 시각 자동 기록
*/
// DB 테이블과 매핑되는 엔티티임을 나타냄
@Entity
@Getter
@Setter
@Table(name = "member") // 테이블 명 지정

@SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 1)
public class MemberDTO {

	// 기본 키 (Primary Key) 설정
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
	private int memberNo; // 회원 고유 번호(PK)

	@Column(length = 20, nullable = false, unique = true)
	private String memberId; // 로그인 아이디 (유니크)

	@Column(length = 255, nullable = false)
	private String memberPw; // 비밀번호 (암호화 저장)

	@Column(length = 10, nullable = false)
	private String memberName; // 이름

	@Column(length = 20, nullable = false)
	private String memberNick; // 닉네임

	@Email
	@Column(nullable = false, unique = true)
	private String memberEmail; // 이메일 (유니크)

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate memberBirth; // 생년월일 (yyyy-MM-dd)

	@Column(length = 300, nullable = false)
	private String memberAddress; // 주소

	@Column(length = 20, nullable = false, unique = true)
	private String memberPhone; // 전화번호 (유니크)

	@Column(length = 1)
	private String memberGender; // 성별 (M/F)

	@CreationTimestamp
	private LocalDateTime memberReg; // 가입일 (생성 시각 자동 저장)

	@Column(length = 500)
	private String memberDeleteReason; // 탈퇴 사유

	@Column(nullable = false)
	private Integer memberRole = 0; // 회원 유형 (0=USER, 1=SELLER, 2=ADMIN, 3=탈퇴회원, 4=RIDER)

	private String autoLoginToken; // 자동 로그인 토큰 (선택)

	// 임시 비밀번호로 로그인한 사용자는 change-pw.html로 페이지 이동 후 비밀번호를 바꾸게 되면 값이 1에서 0으로 바뀜.
	private Integer isTemporary; // 임시 비밀번호 (0=X , 1=임시PW 로그인 상태)

	private Double longitude; // 경도 X longitude

	private Double latitude; // 위도 Y latitude

	@OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AddressBookDTO> addressBooks = new ArrayList<>(); // 배송지 목록 (양방향)

	@OneToMany
	private List<RiderCommunityDTO> riderCommunityDTO; // 라이더 커뮤니티

	@Transient
	private int balance;
}