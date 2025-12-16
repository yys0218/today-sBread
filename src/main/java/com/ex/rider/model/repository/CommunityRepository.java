package com.ex.rider.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.MemberDTO;
import com.ex.rider.model.data.RiderCommunityDTO;

@Repository
public interface CommunityRepository extends JpaRepository<RiderCommunityDTO, Long> {

    // 게시글 최신 10개 가져오기 JPA
    // 엔티티 전체 반환시 와일드카드(*) 사용 불가
    // c 으로 사용사 엔티티 전체 반환
    // SELECT * FROM ( SELECT c.* FROM riderCommunity c ORDER BY c.create_at DESC )
    // WHERE ROWNUM <= 10
    // @Query("select c from RiderCommunityDTO c order by c.createAt desc")
    List<RiderCommunityDTO> findTop10ByOrderByCreateAtDesc();

    Optional<RiderCommunityDTO> findByCommunityNo(Long communityNo);
}