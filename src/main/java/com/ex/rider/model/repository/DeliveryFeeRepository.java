package com.ex.rider.model.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.MemberDTO;
import com.ex.rider.model.data.DeliveryFeeDTO;

@Repository
public interface DeliveryFeeRepository extends JpaRepository<DeliveryFeeDTO, Integer> {

        // 특정 회원의 가장 최근 배달비 1건 조회
        Optional<DeliveryFeeDTO> findTop1ByMemberOrderByFeeNoDesc(MemberDTO member);

        @Query("SELECT f FROM DeliveryFeeDTO f " +
                        "WHERE f.member = :member " +
                        "AND f.feeType = 1 " +
                        "AND f.createdAt BETWEEN :start AND :end")
        List<DeliveryFeeDTO> findByMemberAndFeeTypeAndCreatedAtBetween(@Param("member") MemberDTO member,
                        @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        // 로그인시 사용 해당 회원의 잔액을 가져옴
        @Query(value = """
                        SELECT d.fee_balance
                        FROM delivery_fee d
                        WHERE d.member_member_no = :memberNo
                        ORDER BY d.fee_no DESC
                        FETCH FIRST 1 ROWS ONLY
                        """, nativeQuery = true)
        Integer findLatestFeeBalanceByMemberNo(@Param("memberNo") int memberNo);

        List<DeliveryFeeDTO> findTop10ByMemberAndCreatedAtBetweenOrderByCreatedAtDesc(
                        MemberDTO member,
                        LocalDateTime startTarget,
                        LocalDateTime endTarget);

        List<DeliveryFeeDTO> findByMemberAndCreatedAtBetweenOrderByCreatedAtDesc(
                        MemberDTO member,
                        LocalDateTime startTarget,
                        LocalDateTime endTarget);

}
