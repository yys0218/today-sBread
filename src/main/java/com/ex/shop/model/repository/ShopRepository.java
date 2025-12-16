package com.ex.shop.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.shop.model.data.ShopDTO;

@Repository
public interface ShopRepository extends JpaRepository<ShopDTO, Integer> {

	// 특정 회원번호 ( memberNo ) 로 상점 조회
	// -> 한 회원은 보통 하나의 상점만 가질수 있으므로 단일 SHopDTO 를 반환
	// -> 상품 등록 시 "내 상점" 을 찾을 때 사용됨
	ShopDTO findByMemberNo(int memberNo);

	ShopDTO findTopByMemberNoAndShopRegResult(int memberNo, String shopRegResult);

	// 판매 상태 ( sellStatus ) 에 따라 상점 or 상품 목록 조회
	// 예 : 1 = 판매중 , 0 = 품절 같은 상태값을 조건으로 가져옴
	List<ShopDTO> findBySellStatus(Integer sellStatus);

	// 내 입점 신청 내역 ( 심사중 or 거절된 상태만 조회 )
	// - shopRegResult = Null => 심사 대기중
	// - shopRegResult = "N" => 심사 거절됨
	// memberNo 로 검색 후 , 신청일 ( shopRegAt ) 기준 내림차순 정렬
	// Page<T> 를 반환하므로 페이징 처리 가능
	@Query(" SELECT s FROM ShopDTO s WHERE s.memberNo = :memberNo AND (s.shopRegResult IS NULL OR s.shopRegResult = 'N') ORDER BY s.shopRegAt DESC ")
	Page<ShopDTO> findMyPendingOrRejected(@Param("memberNo") int memberNo, Pageable pageable);

	// shopRegNo로 해당하는 레코드 찾기 (입점 신청 시 사용됨)
	// -> 입점 신청 시 특정 등록 번호로 상점을 조회할 때 사용 예정이었으나 shopNo 로 사용하기로 함
	// ShopDTO findByShopRegNo(int shopRegNo);

	// 상점 고유번호 ( shopNo ) 로 상점 조회
	// -> 보통 PK 조회는 findById() 를 쓰지만 , 네이밍 규칙에 따라 별도로 만들어 본것
	ShopDTO findByShopNo(Integer shopNo);

	// 작업자:윤예솔
	// 상점의 시/도 목록 중복값 제거하여 가져오기
	@Query("select distinct s.shopSido from ShopDTO s order By s.shopSido")
	List<String> findDistinctSido();

	// 작업자:윤예솔
	// 특정 시/도에 해당하는 상점의 시/군/구 목록 중복값 제거하여 가져오기
	@Query("select distinct s.shopSigungu from ShopDTO s where s.shopSido=:sido order by s.shopSigungu")
	List<String> findDistinctSigungu(@Param("sido") String sido);

	// 작업자:윤예솔
	// 특정 시/도, 시/군/구에 해당하는 상점의 동/도로면 목록 중복값 제거하여 가져오기
	@Query("select distinct s.shopBname from ShopDTO s where s.shopSido=:sido and s.shopSigungu=:sigungu order by s.shopBname")
	List<String> findDistinctBname(@Param("sido") String sido, @Param("sigungu") String sigungu);

	// 작성자 : 안병주
	@Query("select s from ShopDTO s where s.memberNo = :memberNo and s.shopRegResult = 'Y'")
	Optional<ShopDTO> optionalFindByMemberNo(@Param("memberNo") int memberNo);

}
