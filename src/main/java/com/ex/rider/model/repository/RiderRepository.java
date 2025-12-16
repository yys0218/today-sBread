package com.ex.rider.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.MemberDTO;
import java.util.List;

@Repository
public interface RiderRepository extends JpaRepository<MemberDTO, Integer> {

    // ajax로 라이더 아이디 조회 JPA
    // 필요한 매개변수 String memberName , String memberEmail
    @Query("select m.memberId from MemberDTO m where m.memberName = :memberName and m.memberEmail = :memberEmail and m.memberRole = 4")
    Optional<String> ajaxFindMemberId(@Param("memberName") String memberName, @Param("memberEmail") String memberEmail);

    // ajax로 라이더 조회 JPA
    // 필요한 매개변수 String memberId , String memberEmail
    @Query("select m from MemberDTO m where m.memberId = :memberId and m.memberEmail = :memberEmail and m.memberRole = 4")
    Optional<MemberDTO> ajaxFindByMemberPw(@Param("memberId") String memberId,
            @Param("memberEmail") String memberEmail);

    Optional<MemberDTO> findByMemberIdAndMemberEmailAndMemberRole(String memberId, String memberEmail, int memberRole);

    // ajax로 라이더 전화번호 조회 JPA
    // 필요한 매개변수 String memberPhone
    @Query("select m.memberPhone from MemberDTO m where m.memberPhone = :memberPhone")
    Optional<String> ajaxFindByMemberPhone(@Param("memberPhone") String memberPhone);

    // ajax로 라이더 이메일 조회 JPA
    // 필요한 매개변수 String memberEmail
    @Query("select m.memberEmail from MemberDTO m where m.memberEmail = :memberEmail")
    Optional<String> ajaxFindByMemberEmail(@Param("memberEmail") String memberEmail);

    Optional<MemberDTO> findByMemberId(String memberId);

    // 라이더 로그인 JPA
    // 엔티티 전체 반환시 와일드카드(*) 사용 불가
    // m 으로 사용사 엔티티 전체 반환
    @Query("select m from MemberDTO m where m.memberId = :memberId and m.memberRole = 4")
    Optional<MemberDTO> riderLogin(@Param("memberId") String memberId);

}
