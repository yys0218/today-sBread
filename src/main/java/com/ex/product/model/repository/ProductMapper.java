package com.ex.product.model.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.data.ProductFileDTO;
import com.ex.product.model.data.ProductOptionDTO;
import com.ex.product.model.data.ProductReviewDTO;
import com.ex.shop.model.data.ShopDTO;

@Mapper
public interface ProductMapper {

	// 상품 작성
	public int insertProduct(ProductDTO productDTO);

	// 작성중인 상품 번호
	public int newProductNo();

	// 파일 업로드
	public int insertFile(ProductFileDTO productFileDTO);

	// 업로드한 파일명 목록
	public ArrayList<String> getFileNames(int productNo);

	// 방금 꺼낸 상품 번호
	// public int getProductNo();

	// 상품 옵션 작성
	public int insertOption(ProductOptionDTO optionDTO);

	// 인기 상품 top10
	public ArrayList<ProductDTO> getPopularList();

	// 시군구별 인기 상품 top10
	public ArrayList<ProductDTO> getSigunguPopularList(String sigungu);

	// 최신 등록 상품 top10
	public ArrayList<ProductDTO> getNewList();

	// 시군구별 최신 등록 상품 top10
	public ArrayList<ProductDTO> getSigunguNewList(String sigungu);

	// 카테고리 이름
	public String getCategoryName(String cate);

	// 카테고리 코드
	public String getCategoryCode(String cate);

	// 전체 상품 목록
	public ArrayList<ProductDTO> getList();

	// (최신순) 시군구별 전체 상품 목록
	public ArrayList<ProductDTO> getSigunguList(String sigungu);

	// 구매순 시군구별 전체 상품 목록
	public ArrayList<ProductDTO> bySalesSigunguList(String sigungu);

	// 별점순 시군구별 전체 상품 목록
	public ArrayList<ProductDTO> byRatingSigunguList(String sigungu);

	// 낮은 가격순 시군구별 전체 상품 목록
	public ArrayList<ProductDTO> byPriceSigunguList(String sigungu);

	// 높은 가격순 시군구별 전체 상품 목록
	public ArrayList<ProductDTO> byPriceDescSigunguList(String sigungu);

	// 동도로별 전체 상품 목록
	// public ArrayList<ProductDTO> getBnameList(@Param("sigungu") String sigungu,
	// @Param("bname") String bname );

	// 카테고리별 상품 목록
	public ArrayList<ProductDTO> getCateList(String category);

	// 카테고리별 서브 상품 목록
	// public ArrayList<ProductDTO> getSubCateList(String sub);

	// (최신순) 시군구별 카테고리별 상품 목록
	public ArrayList<ProductDTO> getSigunguCateList(@Param("category") String category,
			@Param("sigungu") String sigungu);

	// 구매순 시군구별 카테고리 상품 목록
	public ArrayList<ProductDTO> bySalesSigunguCateList(@Param("category") String category,
			@Param("sigungu") String sigungu);

	// 별점순 시군구별 카테고리 상품 목록
	public ArrayList<ProductDTO> byRatingSigunguCateList(@Param("category") String category,
			@Param("sigungu") String sigungu);

	// 낮은 가격순 시군구별 카테고리 상품 목록
	public ArrayList<ProductDTO> byPriceSigunguCateList(@Param("category") String category,
			@Param("sigungu") String sigungu);

	// 높은 가격순 시군구별 카테고리 상품 목록
	public ArrayList<ProductDTO> byPriceDescSigunguCateList(@Param("category") String category,
			@Param("sigungu") String sigungu);

	// 동도로별 카테고리별 상품 목록
	// public ArrayList<ProductDTO> getBnameCateList(@Param("category") String
	// category, @Param("sigungu") String sigungu, @Param("bname") String bname );

	// 상품 상세 조회
	public ProductDTO getProduct(int productNo);

	// 상품 옵션 조회
	public ArrayList<ProductOptionDTO> getProductOptions(int productNo);

	// 특정 상점(shopNo)에 등록된 상품 목록
	ArrayList<ProductDTO> getProductsByShop(int shopNo);

	// 작업자 : 안성진
	// 상품에 해당하는 상점 조회
	public ShopDTO getShop(int shopNo);

	// 작업자 : 안성진
	// 상품 번호로 상점 번호 조회
	Integer getShopNoByProductNo(@Param("productNo") int productNo);

	// 키워드로 상품 검색
	public List<ProductDTO> searchProducts(String keyword);

	public List<String> getRelatedKeywords(String keyword);

	public List<ProductDTO> findByShopNo(int shopNo);

	public ProductDTO getProductByProductNo(int productNo);

	// ---------------------- 상품 수정 및 삭제 매퍼 시작 [ 작업자 : 맹재희 ]
	// ---------------------------------

	// 1. 상품 정보 수정
	// - productNo 를 기준으로 특정 상품 정보를 조회
	public int updateProduct(ProductDTO product);

	// 2. 상품 삭제
	// - productNo 를 기준으로 상품 삭제

	public void deleteProduct(int productNo);
	// ---------------------- 상품 수정 및 삭제 매퍼 끝 [ 작업자 : 맹재희 ]
	// ---------------------------------

	ProductDTO findById(@Param("productNo") int productNo);

	int updateStatus(@Param("productNo") int productNo, @Param("status") int status);

}
