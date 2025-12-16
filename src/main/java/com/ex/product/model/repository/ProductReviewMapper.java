package com.ex.product.model.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import com.ex.product.model.data.ProductReviewCommentDTO;
import com.ex.product.model.data.ProductReviewDTO;

/**
 	ProductReviewMapper
 	- "상품 후기" 도메인의 DB 접근 메서드
 	- 실제 SQL : resources/mappers/ProductReviewMapper.xml 작성
*/
/**
 * 상품 후기 (ProductReview) 관련 DB 접근 인터페이스
 * - MyBatis
 * 
 * 사용 예)
 * - 후기 등록 : insertReview(dto)
 * - 후기 개수 : selectReviewTotal
 * - 후기 평균 : selectAvgRating
 * - 점수별 분포 : selectRatingCounts
 * - 후기 목록 : selectReviewPage
 */
@Mapper
public interface ProductReviewMapper {

	/**
	 * 후기 1건을 DB에 저장
	 * 
	 * @param dto 저장할 후기 데이터 (필수 : productNo, memberNo, reviewCount, rating)
	 *            - reviewNo : DB 시퀀스/자동증가로 생성
	 *            - createdDate : DB에서 기본값으로 생성
	 *            - status : 기본 1로 저장
	 * @return 영향을 받은 행 수 (정상 저장 시 1)
	 * 
	 *         xml 예)
	 *         <insert>
	 *         insert into product_review (review_no, product_no, member_no,
	 *         review_content, rating, create_date, status)
	 *         values (seq_product_review.nextval, #{productNo}, #{memberNo},
	 *         #{reviewContent}, #{rating}, sysdate, 1)
	 *         </insert>
	 */
	int insertReview(ProductReviewDTO dto);

	/**
	 * 특정 상품의 '게시 상태(status = 1)' 후기 총 개수를 조회
	 * 
	 * @param productNo 상품 번호
	 * @return 총 개수
	 *         - 상세 페이지의 "총 후기 개수" 표시에 사용됨
	 */
	int selectReviewTotal(@Param("productNo") int productNo);

	/**
	 * 특정 상품의 '게시 상태(status = 1)' 후기의 평균 별점 조회
	 * 
	 * @param productNo 상품 번호
	 * @return 평균 별점(소수점), 후기가 하나도 없으면 Null이 반환되어 Double로 사용함
	 *         - 상세 페이지의 "평균 벌점" 표시에 사용됨
	 *         - 서비스 단에서 null을 0.0 등의 기본값으로 처리
	 */
	Double selectAvgRating(@Param("productNo") int productNo);

	/**
	 * 특정 상품의 '게시 상태(status = 1)' 후기 점수별 개수를 조회
	 * 
	 * @param productNo 상품 번호
	 * @return 각 행이 "점수"와 "개수" 정보를 담은 Map의 리스트
	 */
	List<Map<String, Object>> selectRatingCounts(@Param("productNo") int productNo);

	/**
	 * 특정 회원이 작성한 후기 목록을 페이징하여 조회
	 */
	List<ProductReviewDTO> selectReviewsByMember(@Param("memberNo") int memberNo,
			@Param("offset") int offset,
			@Param("size") int size);

	int countReviewsByMember(@Param("memberNo") int memberNo);

	// 상품별 후기 페이지 (최신순, status = 1)
	List<ProductReviewDTO> selectReviewPageByProduct(@Param("productNo") int productNo,
			@Param("offset") int offset,
			@Param("size") int size);

	// 1. 상점 리뷰 조회
	List<ProductReviewDTO> selectReviewsByShop(@Param("shopNo") int shopNo);

	// 2. 리뷰별 댓글 조회 (컨트롤러에서 Map<Long, List<DTO>>로 사용)
	List<ProductReviewCommentDTO> selectCommentsByReview(@Param("reviewNo") int reviewNo);

	// 3. 상점별 모든 댓글 조회 (필요 시)
	List<ProductReviewCommentDTO> selectCommentsByShop(@Param("shopNo") int shopNo);

	// 4. 판매자 댓글 등록
	int insertReviewComment(@Param("reviewNo") int reviewNo,
			@Param("shopNo") int shopNo,
			@Param("content") String content,
			@Param("createdAt") LocalDateTime createdAt);

	// 상품별 리뷰 조회 (회원 정보, 상점 이름 포함)
	List<ProductReviewDTO> selectReviewsByProductNo(@Param("productNo") int productNo);

	// 상점별 리뷰 조회 (회원 정보, 상품 이름 포함)
	List<ProductReviewDTO> selectReviewsByShopNo(@Param("shopNo") int shopNo);

	// 상점별 모든 리뷰 내용 조회
	List<String> selectReviewContentsByShop(@Param("shopNo") int shopNo);

	void insertCommentSeller(ProductReviewCommentDTO comment);

	public List<ProductReviewDTO> selectReviewWithComments(@Param("shopNo") int shopNo);

	public List<ProductReviewDTO> selectProductReviewWithComments(@Param("productNo") int productNo);

}
