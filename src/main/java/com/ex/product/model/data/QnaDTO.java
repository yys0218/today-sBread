package com.ex.product.model.data;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class QnaDTO {	// 회원이 판매자에게 문의 DTO
	
	private int boardNo;			// 게시글 번호
	
	private String boardTitle;		// 게시글 제목
	
	private String boardContent;	// 게시글 내용
	
	private String memberId;		// 회원 아이디
	
	private int memberNo;			// 회원 고유 식별 번호
	
	private LocalDateTime reg;		// 작성일
	
	private int status;				// 게시글 등록 상태 - 1: 게시중 / 2: 삭제 / 3: 비밀글
	
	private int isAnswered;			// 답변 여부 - 1 : 미답변, 2 : 답변 완료
	
	private int shopNo;				// 상점 고유 식별 번호
	
	private int productNo;			// 상품 고유 식별 번호
}
