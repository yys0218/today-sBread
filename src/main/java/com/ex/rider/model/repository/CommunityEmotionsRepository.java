package com.ex.rider.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.MemberDTO;
import com.ex.rider.model.data.CommunityEmotionsDTO;
import com.ex.rider.model.data.RiderCommunityDTO;

@Repository
public interface CommunityEmotionsRepository extends JpaRepository<CommunityEmotionsDTO, Integer> {

    // 특정 게시글과 회원으로 조회 (이미 감정 표현했는지 체크용)
    CommunityEmotionsDTO findByCommunityAndMember(RiderCommunityDTO community, MemberDTO member);

    // 특정 게시글의 모든 감정 조회
    List<CommunityEmotionsDTO> findByCommunity(RiderCommunityDTO community);

    // 필요하면 "좋아요만" 또는 "싫어요만" 카운트
    int countByCommunityAndEmotionsType(RiderCommunityDTO community, int emotionsType);
}
