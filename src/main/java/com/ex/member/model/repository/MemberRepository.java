package com.ex.member.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ex.member.model.data.MemberDTO;


// 회원 정보를 DB와 연결하는 Repository
// JpaRepository를 상속하여 기본 CRUD 제공
// 아이디, 이메일, 닉네임으로 조회할 수 있는 메서드 정의
@Repository
public interface MemberRepository extends JpaRepository<MemberDTO, Integer>{
	
	// ===== 회원 조회 =====
	// 아이디로 회원 조회
	// 매개변수 : memberId
	Optional<MemberDTO> findByMemberId(String memberId);
	
	// 이메일로 회원 조회
	// 매개변수 : memberEmail
	Optional<MemberDTO> findByMemberEmail(String memberEmail);
	
	// 닉네임으로 회원 조회
	// 매개변수 : memberNick
	Optional<MemberDTO> findByMemberNick(String memberNick);
	
	// ===== 아이디 찾기 =====
	// 이름 + 이메일로 회원 조회
	// 매개변수 : memberName, memberEmail
	Optional<MemberDTO> findByMemberNameAndMemberEmail(String memberName, String memberEmail);
	
	// ===== 비밀번호 찾기 =====
	// 아이디 + 이름 + 이메일로 회원 조회
	// 매개변수 : memberId, memberName, memberEmail
	Optional<MemberDTO> findByMemberIdAndMemberNameAndMemberEmail(String memberId, String memberName, String memberEmail);
	
	// ===== 비밀번호 찾기 =====
	// 회원 비밀번호를 업데이트 (임시 비밀번호 발급용)
	// @Modifying : INSERT, UPDATE, DELETE 같은 쿼리 수행
	// @Transactional : 트랜잭션 적용 (DB 반영)
	// 매개변수 : memberId, memberPw
	@Modifying
	@Transactional
	@Query("update MemberDTO m set m.memberPw = :memberPw where m.memberId = :memberId")
	public void updateMemberPassword(@Param("memberId") String memberId, 
									 @Param("memberPw") String memberPw);
	
	// 마이페이지 - 회원 정보 수정 -> 전화번호로 회원 조회
	Optional<MemberDTO> findByMemberPhone(String memberPhone);
	
	// ===== 비밀번호 변경 =====
	// 로그인 직후 새 비밀번호를 입력하면 기존 비밀번호를 새 비밀번호로 교체
	// @Modifying, @Transactional 동일하게 필요
	// 매개변수 : memberId, newPw
	// 사용처 : 사용자가 임시 비밀번호로 로그인 후 비밀번호 변경 화면에서 호출
//	@Modifying
//	@Transactional
//	@Query("update MemberDTO m set m.memberPw = :newPw where m.memberId = :memberId")
//	public void changePassword(@Param("memberId") String memberId,
//							   @Param("newPw") String newPw);
	
	// ===== 자동 로그인 =====
	// 자동 로그인 토큰으로 회원 조회
	// 매개변수 : token
	Optional<MemberDTO> findByAutoLoginToken(String token);
	
	
	// ===== 회원 탈퇴 =====
	// memberId 기준으로 회원 삭제
	// JPA의 기본 deleteById() 메서드는 PK(memberNo)만 사용 가능
	// 회원 탈퇴 시 식별자는 memberId 이므로 JPQL(@Query)로 커스텀 DELETE 쿼리를 작성
	@Modifying		// DELETE, UPDATE 같은 변경 쿼리 수행 시 필요
	@Transactional	// 변경 쿼리는 반드시 트랜잭션 내에서 수행해야 함
	@Query("delete from MemberDTO m where m.memberId = :memberId")
	public void deleteByMemberId(@Param("memberId") String memberId);
	
	// 회원번호로 회원 조회
	// 매개변수 : memberNo
	MemberDTO findByMemberNo(int memberNo);
	
	// ===== 탈퇴 처리 =====
	// 실제 삭제 대신 '탈퇴 상태'로 마킹 (memberRole = 3)
	// 자동 로그인 토큰 제거(null로 바꿈), 임시비번 해제(0으로 바꿈), 탈퇴 사유 저장
	// 전화번호/이메일/닉네임은 업데이트 하지 않음 -> 2차 오류 방지 (길이 초과 등 DTO 관련)
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update MemberDTO m set m.memberDeleteReason = :reason, m.memberRole = 3, m.autoLoginToken = null, m.isTemporary = 0 where m.memberId = :memberId")
	int markWithdrawn(@Param("memberId") String memberId, @Param("reason") String reason);
	
	// 활성 회원 전용 조회 (탈퇴 제외)
	// - findActiveById : 탈퇴 (memberRole = 3) 제외하고 아이디로 조회
	// 회원 탈퇴 후 재로그인 방지에 사용 (로그인 경로에서 이 메서드만 사용)
	@Query("select m from MemberDTO m where m.memberId = :id and m.memberRole <> 3")
	Optional<MemberDTO> findActiveById(@Param("id") String id);
}