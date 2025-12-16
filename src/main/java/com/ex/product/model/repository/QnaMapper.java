package com.ex.product.model.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ex.product.model.data.QnaDTO;

@Mapper
public interface QnaMapper {
	
	// 등록 (boardNo)
	int insert(QnaDTO dto);
	
	// 단건 조회
	QnaDTO findById(@Param("boardNo") int boardNo);
	
	// 상품별 목록 + 카운트 (상품 상세 페이지용)
	List<QnaDTO> findByProduct(@Param("productNo") int productNo,
							   @Param("offset") int offset,
							   @Param("size") int size);
	int countByProduct(@Param("productNo") int productNo);
	
	// 상점별 목록 + 카운트 (판매자 QnA 페이지용)
	List<QnaDTO> findByShop(@Param("shopNo") int shopNo,
							@Param("offset") int offset,
							@Param("size") int size);
	
	int countByShop(@Param("shopNo") int shopNo);
	
	// 상태 / 비밀글 / 답변 여부 업데이트
	// status = 2 : 삭제
	int delete(@Param("boardNo") int boardNo);
	
	// status = 3 : 비밀글
	int secret(@Param("boardNo") int boardNo);
	
	// status = 1 or 2
	int updateAnswered(@Param("boardNo") int boardNo,
					   @Param("isAnswered") int isAnswered);
	
	// 마이페이지 - 내 문의 목록 (memberNo + 페이징 + 상태/검색)
	List<QnaDTO> findMyQna(@Param("memberNo") int memberNo,
						   @Param("offset") int offset,
						   @Param("size") int size,
						   @Param("status") String status,	// "WAIT" | "DONE" | null /"" = 전체
						   @Param("q") String q);	// q : 검색어
	
	// 마이페이지 - 문의 총 건수
	int countMyQna(@Param("memberNo") int memberNo,
				   @Param("status") String status,
				   @Param("q") String q);
	
	// 유저용 판매자 페이지 문의
//	List<QnaDTO> selectQuestionsByShop(int shopNo);
}
