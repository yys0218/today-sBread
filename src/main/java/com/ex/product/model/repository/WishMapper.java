package com.ex.product.model.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ex.product.model.data.WishDTO;
/**
	찜(Wish) 테이블에 접근하는 MyBatis Mapper 인터페이스
	
	마이페이지 '찜 목록' 조회/카운트/삭제용 메서드
	상품 상세 페이지 토글/상태 확인 메서드
	
*/
@Mapper
public interface WishMapper {
	
	// ===== 마이페이지 '내 찜 목록' 페이지네이션 조회 =====
	// 내 찜 목록 조회
	// memberNo, offset, limit
	List<WishDTO> selectMemberWishList(
		@Param("memberNo") int memberNo,
		@Param("offset") int offset,
		@Param("limit") int limit
	);
	
	// 내 찜 총 개수
	int countMemberWishList(@Param("memberNo") int memberNo);
	
	// 찜 선택 삭제 - 체크박스 일괄 삭제
	// memberNo, wishNos
	int deleteByWishNos(
		@Param("memberNo") int memberNo,
		@Param("wishNos") List<Integer> wishNos
	);
	
	// 단건 삭제 - 개별 '삭제'버튼
	int deleteByWishNo(
		@Param("memberNo") int memberNo,
		@Param("wishNo") int wishNo
	);
	// ===== 마이페이지 '내 찜 목록' 페이지네이션 조회 종료 =====
	
	
	// ===== 토글 / 상태 확인 (상품 상세에서 사용) =====
	// 찜 추가
	// WishDTO
	int insertWish(WishDTO dto);
	
	// 찜 삭제
	// memberNo, productNo
	int deleteWish(@Param("memberNo") int memberNo, @Param("productNo") int productNo);
	
	// 해당 상품을 내가 찜했는지 여부 (0/1)
	// memberNo, productNo
	int exists(@Param("memberNo") int memberNo, @Param("productNo") int productNo);
	
	// 상품별 찜 수
	// productNo
	int countByProduct(@Param("productNo") int productNo);
	// ===== 토글 / 상태 확인 완료 =====
	
	// 장바구니 upsert (있으면 수량+=qty, 없으면 insert)
	int upsertCart(
		@Param("memberNo") int memberNo,
		@Param("productNo") int productNo,
		@Param("qty") int qty
	);
	
	
	// productNo 기준으로 '내 찜 PK' 조회
	// select wish_no from wish where member_no=? and product_no=?
	Integer findWishNo(
		@Param("memberNo") int memberNo,
		@Param("productNo") int productNo
	);
}