package com.ex.product.model.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.product.model.data.QnaDTO;
import com.ex.product.model.repository.QnaMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QnaService {

	private final QnaMapper qnaMapper;
	private final ProductService productService;
	
	
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	// 등록 (상품/상점 동시 반영)
	// productNo, memberNo, memberId, title, content, secret
	// 상품 번호로 상점 번호(shopNo)를 알아내 DB에 같이 저장함
	// 등록 직후 상품 상세 문의 목록과 판매자 (QnA) 목록 양쪽에서 보이게 됨
	@Transactional
	public int createQna(int productNo, int memberNo, String memberId, String title, String content, boolean secret) {
		int shopNo = safeGetShopNo(productNo);	// 상품 -> 상점 번호 조회
		if(shopNo <= 0) {
			throw new IllegalArgumentException("상점 정보를 찾을 수 없습니다. (productNo=" +productNo + ")");
		}
		
		QnaDTO dto = new QnaDTO();
		dto.setProductNo(productNo);
		dto.setShopNo(shopNo);
		dto.setMemberNo(memberNo);
		dto.setMemberId(memberId);
		
		dto.setBoardTitle(nullToEmpty(title));
		dto.setBoardContent(nullToEmpty(content));
		
		// status : 1=게시중 , 2=삭제 , 3=비밀글
		dto.setStatus(secret ? 3 : 1);
		
		// is_answered : 1=미답변 , 2=답변 완료
		dto.setIsAnswered(1);
		
		// insert 실행
		qnaMapper.insert(dto);
		
		return dto.getBoardNo();
	}
	
	// 단건 조회
	@Transactional(readOnly = true)
	public QnaDTO getQna(int boardNo) {
		return qnaMapper.findById(boardNo);
	}
	
	// 상품 상세 페이지용 목록/카운트
	// 상품 별 QnA 목록 (최신순, 페이징)
	// productNo, page, size, QNaPage
	@Transactional(readOnly = true)
	public QnaPage getProductQnaPage(int productNo, int page, int size) {
		int p = Math.max(1, page);
		int s = Math.max(1, size);
		int offset = (p - 1) * s;
		
		List<QnaDTO> items = qnaMapper.findByProduct(productNo, offset, s);
		int total = qnaMapper.countByProduct(productNo);
		
		return new QnaPage(items, total, p, s);
	}
	
	// 판매자 QnA 페이지용 목록/카운트
	// 상점별 QnA 목록 (최신순, 페이징)
	// shopNo, page, size
	@Transactional(readOnly = true)
	public QnaPage getShopQnaPage(int shopNo, int page, int size) {
		int p = Math.max(1, page);
		int s = Math.max(1, size);
		int offset = (p - 1) * s;
		
		List<QnaDTO> items = qnaMapper.findByShop(shopNo, offset, s);
		int total = qnaMapper.countByShop(shopNo);
		
		return new QnaPage(items, total, p, s);
	}
	
	// 상태 변경 (삭제 / 비밀글) & 답변 여부
	// 삭제
	@Transactional
	public boolean delete(int boardNo) {
		return qnaMapper.delete(boardNo) > 0;
	}
	
	// 비밀글 전환
	@Transactional
	public boolean secret(int boardNo) {
		return qnaMapper.secret(boardNo) > 0;
	}
	
	// 답변 여부 변경
	// boardNo, answered
	@Transactional
	public boolean answered(int boardNo, boolean answered) {
		int code = answered ? 2 : 1;
		return qnaMapper.updateAnswered(boardNo, code) > 0;
	}
	

	// 상품 번호로 shopNo를 가져옴
	private int safeGetShopNo(int productNo) {
		try {
			var p = productService.product(productNo);
			return (p != null && p.getShopNo() > 0) ? p.getShopNo() : 0;
		}catch(Exception e) {
			return 0;
		}
	}
	
	private static String nullToEmpty(String s) {
		return (s == null) ? "" : s;
	}
	
	// 페이징 결과 DTO
	// items : 목록 / total : 전체 건수 / page, size : 현재 페이지, 페이지 크기 / hasMore : 다음 페이지가 있는지 여부
	public record QnaPage(List<QnaDTO> items, int total, int page, int size) {
		public boolean hasMore() {
			return page * size < total;
		}
	}
}
