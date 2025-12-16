package com.ex.admin.model.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.MemberDTO;

@Repository
public interface AdminMemberRepository extends JpaRepository<MemberDTO, Integer>, JpaSpecificationExecutor<MemberDTO> {

       @Query("select m from MemberDTO m where m.memberId = :memberId and m.memberRole = 2")
       Optional<MemberDTO> adminLogin(@Param("memberId") String memberId);

       @Query("select m.memberRole from MemberDTO m where m.memberNo = :memberNo")
       int findMemberRoleByMemberNo(@Param("memberNo") int memberNo);

       @Query("SELECT m FROM MemberDTO m " +
                     "WHERE (:searchKeyword IS NULL OR m.memberId LIKE %:searchKeyword% OR m.memberNick LIKE %:searchKeyword%) "
                     +
                     "AND (:roleValue IS NULL OR m.memberRole = :roleValue)")
       Page<MemberDTO> searchMembers(@Param("roleValue") Integer roleValue,
                     @Param("searchKeyword") String searchKeyword, Pageable pageable);

       Optional<MemberDTO> findByMemberNick(String memberNick);

       // 기간별 신규 가입자 수
       @Query("SELECT COUNT(m) FROM MemberDTO m " +
                     "WHERE m.memberReg BETWEEN :start AND :end")
       long countNewMembersBetween(@Param("start") LocalDateTime start,
                     @Param("end") LocalDateTime end);

       // 일자별 신규 가입자 수
       @Query("SELECT FUNCTION('TO_CHAR', m.memberReg, 'YYYY-MM-DD'), COUNT(m) " +
                     "FROM MemberDTO m " +
                     "WHERE m.memberReg BETWEEN :start AND :end " +
                     "GROUP BY FUNCTION('TO_CHAR', m.memberReg, 'YYYY-MM-DD') " +
                     "ORDER BY FUNCTION('TO_CHAR', m.memberReg, 'YYYY-MM-DD')")
       List<Object[]> countNewMembersPerDay(@Param("start") LocalDateTime start,
                     @Param("end") LocalDateTime end);
}