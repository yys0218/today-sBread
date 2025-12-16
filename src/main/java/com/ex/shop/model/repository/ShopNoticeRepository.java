package com.ex.shop.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ex.shop.model.data.ShopNoticeDTO;

/**
 * ShopNoticeRepository
 * --------------------------------------------------------------------------------------------------------
 * 판매자 상점의 알림사항(ShopNotice) 관련 JPA Repository
 * - 알림사항 조회, 단건 조회
 */
public interface ShopNoticeRepository extends JpaRepository<ShopNoticeDTO, Integer> {

    // ======================= 알림사항 조회 =======================

    /**
     * 상점 번호(shopNo) 기준 알림 목록 조회 (최신 작성순)
     *
     * @param shopNo 상점 번호
     * @return List<ShopNoticeDTO> 최신순으로 정렬된 알림 목록
     */
    List<ShopNoticeDTO> findAllByShopNoOrderByCreatedAtDesc(Integer shopNo);

    /**
     * 상점 번호 기준 최신 공지 조회
     * - pinned가 true인 공지가 있으면 우선 반환
     * - 없으면 최신 작성 공지 반환
     *
     * @param shopNo 상점 번호
     * @return Optional<ShopNoticeDTO> 단건 공지
     */
    Optional<ShopNoticeDTO> findFirstByShopNoOrderByPinnedDescCreatedAtDesc(Integer shopNo);

    /**
     * 공지 단건 조회
     *
     * @param shopNoticeNo 공지 번호
     * @param shopNo 상점 번호
     * @return Optional<ShopNoticeDTO> 존재하면 공지 반환
     */
    Optional<ShopNoticeDTO> findByShopNoticeNoAndShopNo(Integer shopNoticeNo, Integer shopNo);
}

