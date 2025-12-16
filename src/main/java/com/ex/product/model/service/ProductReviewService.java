package com.ex.product.model.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.product.model.data.ProductReviewCommentDTO;
import com.ex.product.model.data.ProductReviewDTO;
import com.ex.product.model.repository.ProductReviewMapper;

import lombok.RequiredArgsConstructor;

/**
 * 상품 후기 관련 비즈니스 로직을 담당하는 서비스 클래스
 * 
 * - 컨트롤러에서 전달한 DTO를 검증하고 보정함.
 * - MyBatis Mapper를 호출해 DB에 저장/조회함.
 * - 상세 페이지에서 사용할 리뷰 요약 (평균, 총개수, 히스토그램용 분포)을 계산해서 반환함.
 * 
 * 트랜잭션
 * - 쓰기(addReview)는 @Transactional 로 묶어 하나라도 실패 시 전체 롤백
 * - 읽기(getSummary)는 @Transactional(readOnly = true)로 성능 최적화와 안정성을 확보
 * 
 * 예외 처리
 * - 잘못된 입력(상품번호/회원번호/내용/별점)에는 IllegalArgumentException을 던짐
 * - 컨트롤러에서 적절한 HTTP 응답(400 Bad Request 등)으로 변환하여 사용자에게 전달
 */
@Service
@RequiredArgsConstructor
public class ProductReviewService {

	private final ProductReviewMapper reviewMapper;

	/**
	 * 후기 등록
	 * 처리 단계
	 * 1) 필수 값 검증 : productNo, memberNo, reviewContent, rating
	 * 2) 내용 공백 제거 및 빈 문자열 거부
	 * 3) 별점이 1~5 범위를 벗어나면 강제로 보정
	 * 4) Mapper를 통해 DB insert 수행
	 */
	@Transactional
	public int addReview(ProductReviewDTO dto) {
		// null 체크
		if (dto == null)
			throw new IllegalArgumentException("리뷰 데이터가 없습니다.");
		if (dto.getOrderNo() <= 0)
			throw new IllegalArgumentException("주문 번호가 올바르지 않습니다.");
		// 상품/회원 번호 검증
		if (dto.getProductNo() <= 0)
			throw new IllegalArgumentException("상품 번호가 올바르지 않습니다.");
		if (dto.getMemberNo() <= 0)
			throw new IllegalArgumentException("회원 번호가 올바르지 않습니다.");

		// 내용 공백 제거 후 재확인
		String content = dto.getReviewContent() == null ? "" : dto.getReviewContent().trim();
		if (content.isEmpty())
			throw new IllegalArgumentException("후기 내용을 입력해주세요.");
		dto.setReviewContent(content);

		// 별점 보정 (1 미만 : 1, 5 초과 : 5)
		int rating = dto.getRating();
		if (rating < 1)
			rating = 1;
		if (rating > 5)
			rating = 5;
		dto.setRating(rating);

		// insert 실행
		return reviewMapper.insertReview(dto);
	}

	/**
	 * 특정 상품의 리뷰 요약 정보 조회
	 * 반환 : total, avg, ownerReplies, counts
	 * 처리 단계
	 * 1) 상품 번호 검증
	 * 2) 총 개수/평균 별점 조회
	 * 3) 점수별 개수 조회 후, 1~5 모든 키가 존재하도록 맵 구성 (없으면 0)
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> getSummary(int productNo) {
		// 상품 번호 검증
		if (productNo <= 0)
			throw new IllegalArgumentException("상품 번호가 올바르지 않습니다.");

		// 총 개수/평균 조회
		int total = reviewMapper.selectReviewTotal(productNo);
		Double avgObj = reviewMapper.selectAvgRating(productNo);
		double avg = (avgObj == null) ? 0.0 : avgObj.doubleValue();

		// 점수별 개수 초기화
		// LinkedHashMap을 사용해 1~5 순서를 유지함
		Map<String, Integer> counts = new LinkedHashMap<>();
		for (int i = 1; i <= 5; i++)
			counts.put(String.valueOf(i), 0);

		// DB에서 점수별 개수 목록을 조회 후 맵에 반영
		// - Mapper xml에서 score, cnt 별칭을 사용
		// - DB/드라이버에 따라 키가 대문자 ("SCORE"/"CNT")로 들어오는 경우도 대비
		List<Map<String, Object>> rows = reviewMapper.selectRatingCounts(productNo);
		for (Map<String, Object> row : rows) {
			Object scoreObj = row.get("score") != null ? row.get("score") : row.get("SCORE");
			Object cntObj = row.get("cnt") != null ? row.get("cnt") : row.get("CNT");

			int score = toInt(scoreObj);
			int cnt = toInt(cntObj);
			// 1~5 범위만 반영
			if (score >= 1 && score <= 5) {
				counts.put(String.valueOf(score), cnt);
			}
		}

		// 최종 결과 맵 구성
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("total", total);
		result.put("avg", avg);
		result.put("ownerReplies", 0); // 사장님 댓글 기능이 없으면 0 고정
		result.put("counts", counts);
		return result;
	}

	/**
	 * 안전하게 Object -> int로 반환하는 헬퍼 메서드
	 * - 숫자 타입(Number)이면 그대로 intValue()
	 * - 그 외 문자열이면 파싱 시도
	 * - 실패/null 이면 0 반환
	 */
	private int toInt(Object o) {
		if (o == null)
			return 0;
		if (o instanceof Number)
			return ((Number) o).intValue();
		try {
			return Integer.parseInt(String.valueOf(o));
		} catch (Exception e) {
			return 0;
		}
	}

	// 마이페이지 : 회원 후기 페이징
	@Transactional(readOnly = true)
	public Page<ProductReviewDTO> findPageByMember(int memberNo, Pageable pageable) {
		if (memberNo <= 0)
			throw new IllegalArgumentException("회원 번호가 올바르지 않습니다.");

		int page = pageable.getPageNumber();
		int size = pageable.getPageSize();
		int offset = page * size;

		List<ProductReviewDTO> list = reviewMapper.selectReviewsByMember(memberNo, offset, size);
		int total = reviewMapper.countReviewsByMember(memberNo);

		return new PageImpl<>(list, pageable, total);
	}

	// 상품 상세 페이지 : 후기

	// 특정 상품의 게시 리뷰 총 개수
	@Transactional(readOnly = true)
	public int totalByProduct(int productNo) {
		if (productNo <= 0)
			throw new IllegalArgumentException("상품 번호가 올바르지 않습니다.");
		return reviewMapper.selectReviewTotal(productNo);
	}

	// 특정 상품의 평균 별점 (리뷰 없으면 0.0)
	@Transactional(readOnly = true)
	public double avgByProduct(int productNo) {
		if (productNo <= 0)
			throw new IllegalArgumentException("상품 번호가 올바르지 않습니다.");
		return reviewMapper.selectAvgRating(productNo) == null ? 0.0 : reviewMapper.selectAvgRating(productNo);
	}

	// 점수별 분포
	@Transactional(readOnly = true)
	public List<Map<String, Object>> ratingCounts(int productNo) {
		if (productNo <= 0)
			throw new IllegalArgumentException("상품 번호가 올바르지 않습니다.");
		return reviewMapper.selectRatingCounts(productNo);
	}

	// 특정 상품의 리뷰 페이징 목록
	@Transactional(readOnly = true)
	public List<ProductReviewDTO> findPageByProduct(int productNo, int page, int size) {
		if (productNo <= 0)
			throw new IllegalArgumentException("상품 번호가 올바르지 않습니다.");
		int safePage = Math.max(1, page);
		int safeSize = Math.max(1, size);
		int offset = (safePage - 1) * safeSize;
		return reviewMapper.selectReviewPageByProduct(productNo, offset, safeSize);
	}

	// 상점별 리뷰 조회
	@Transactional(readOnly = true)
	public List<ProductReviewDTO> getReviewsByShop(int shopNo) {
		if (shopNo <= 0)
			throw new IllegalArgumentException("상점 번호가 올바르지 않습니다.");
		return reviewMapper.selectReviewsByShop(shopNo);
	}

	public void saveReply(Integer reviewNo, Integer shopNo, String content) {
		reviewMapper.insertReviewComment(reviewNo, shopNo, content, LocalDateTime.now());
	}

	// 댓글 저장
	public void saveComment(ProductReviewCommentDTO comment) {
		reviewMapper.insertCommentSeller(comment);
	}
}
