package com.ex.product.model.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.data.ProductFileDTO;
import com.ex.product.model.data.ProductOptionDTO;
import com.ex.product.model.data.WishDTO;
import com.ex.product.model.repository.ProductMapper;
import com.ex.product.model.repository.ProductReviewMapper;
import com.ex.product.model.repository.WishMapper;
import com.ex.shop.model.data.ShopDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	private final ProductMapper productMapper;
	private final WishMapper wishMapper;
	private final ProductReviewMapper productReviewMapper;

	// 상품 작성
	public int insert(ProductDTO productDTO) {
		return productMapper.insertProduct(productDTO);
	}

	// 작성 중인 상품의 상품 번호 조회
	public int newProductNo() {
		return productMapper.newProductNo();
	}

	// 파일 업로드
	public int insertFile(ProductFileDTO productFileDTO) {
		return productMapper.insertFile(productFileDTO);
	}

	public void dummyFile() {
		// <insert id="insertFile">
		// insert into PRODUCT_FILE values(PRODUCT_FILE_SEQ.NEXTVAL, #{productNo},
		// #{fileName}, #{filePath}, sysdate)
		// </insert>
		for (int i = 35; i < 544; i++) {
			ProductFileDTO dto = new ProductFileDTO();
			dto.setFileName("default.png");
			dto.setFilePath("/upload/product/");
			dto.setProductNo(i);
			productMapper.insertFile(dto);
		}
	}

	// 업로드한 파일 이름 목록
	public ArrayList<String> getFileNames(int productNo) {

		return productMapper.getFileNames(productNo);
	}

	// 가장 최근 등록된 상품 번호 꺼내기
	// public int getProductNo() {
	// return productMapper.getProductNo();
	// }

	// 상품 옵션 작성
	//public int insertOption(ProductOptionDTO optionDTO) {
	//	return productMapper.insertOption(optionDTO);
	//}
	
	//상품 옵션 조회
	//public ArrayList<ProductOptionDTO> getOptions(int productNo){
	//	return productMapper.getProductOptions(productNo);
	//}
	
	public void dummyProduct() {
		Random random = new Random();

		// 카테고리별 상품명 풀
		List<String> breadList = List.of("크루아상", "바게트", "소금빵", "식빵", "치아바타");
		List<String> cakeList = List.of("티라미수", "치즈케이크", "가토쇼콜라", "레드벨벳", "당근케이크");
		List<String> sandwichList = List.of("클럽샌드위치", "햄치즈샌드위치", "치킨샐러드", "시저샐러드");
		List<String> dessertList = List.of("마카롱", "푸딩", "초콜릿", "젤라또", "쿠키");

		// 카테고리별 상품명-HTML 상세 설명 매핑
		Map<String, String> breadMap = Map.of(
				"크루아상",
				"<h3>바삭한 크루아상</h3><p>매일 아침 구워낸 버터 풍미 가득한 크루아상입니다.</p><img src='/upload/product/sample/croissant.jpg'>",
				"바게트", "<h3>정통 바게트</h3><p>겉은 바삭, 속은 쫄깃한 정통 프랑스 바게트.</p>",
				"소금빵", "<h3>소금빵</h3><p>짭짤한 소금과 버터가 조화를 이루는 고소한 빵.</p>",
				"식빵", "<h3>담백한 식빵</h3><p>아침에 부담 없이 즐길 수 있는 담백한 식빵.</p>",
				"치아바타", "<h3>치아바타</h3><p>이탈리아 정통 스타일의 고소한 치아바타.</p>");

		Map<String, String> cakeMap = Map.of(
				"티라미수", "<h3>티라미수</h3><p>에스프레소와 마스카포네 치즈가 어우러진 부드러운 케이크.</p>",
				"치즈케이크", "<h3>치즈케이크</h3><p>꾸덕꾸덕하고 진한 치즈의 풍미가 가득한 케이크.</p>",
				"가토쇼콜라", "<h3>가토 쇼콜라</h3><p>진한 초콜릿 맛의 프랑스 전통 케이크.</p>",
				"레드벨벳", "<h3>레드벨벳</h3><p>부드러운 크림치즈와 붉은 케이크가 조화로운 디저트.</p>",
				"당근케이크", "<h3>당근케이크</h3><p>향긋한 시나몬과 당근이 어우러진 건강 케이크.</p>");

		Map<String, String> sandwichMap = Map.of(
				"클럽샌드위치", "<h3>클럽샌드위치</h3><p>햄, 치즈, 야채가 가득 들어간 든든한 샌드위치.</p>",
				"햄치즈샌드위치", "<h3>햄치즈샌드위치</h3><p>부드러운 햄과 고소한 치즈가 어우러진 샌드위치.</p>",
				"치킨샐러드", "<h3>치킨샐러드</h3><p>신선한 야채와 담백한 닭가슴살을 곁들인 샐러드.</p>",
				"시저샐러드", "<h3>시저샐러드</h3><p>로메인, 크루통, 치즈가 어우러진 클래식 샐러드.</p>");

		Map<String, String> dessertMap = Map.of(
				"마카롱", "<h3>마카롱</h3><p>바삭한 꼬끄와 달콤한 필링이 조화를 이루는 프랑스 디저트.</p>",
				"푸딩", "<h3>푸딩</h3><p>부드럽고 달콤한 푸딩, 입안에서 사르르 녹습니다.</p>",
				"초콜릿", "<h3>초콜릿</h3><p>달콤쌉싸름한 매력을 가진 수제 초콜릿.</p>",
				"젤라또", "<h3>젤라또</h3><p>이탈리아 전통 방식으로 만든 신선한 아이스크림.</p>",
				"쿠키", "<h3>쿠키</h3><p>바삭하고 고소한 수제 쿠키.</p>");

		// 상점 번호 4 ~ 20
		for (int shopNo = 4; shopNo <= 20; shopNo++) {
			// 판매자 번호 = 상점번호 + 19
			int memberNo = shopNo + 19;

			for (int i = 1; i <= 30; i++) {
				ProductDTO dto = new ProductDTO();

				int productNo = this.newProductNo();
				// 상품번호 (고유하게 shopNo*1000+i)
				dto.setProductNo(productNo);

				// 카테고리 랜덤 선택
				int categoryType = random.nextInt(4); // 0~3
				String productName = "";
				String productInfo = "";

				switch (categoryType) {
					case 0 -> {
						dto.setCategoryMain("br");
						dto.setCategorySub("br" + (random.nextInt(5) + 1));
						productName = breadList.get(random.nextInt(breadList.size()));
						productInfo = breadMap.get(productName);
					}
					case 1 -> {
						dto.setCategoryMain("cake");
						dto.setCategorySub("cake" + (random.nextInt(3) + 1));
						productName = cakeList.get(random.nextInt(cakeList.size()));
						productInfo = cakeMap.get(productName);
					}
					case 2 -> {
						dto.setCategoryMain("ss");
						dto.setCategorySub("ss" + (random.nextInt(2) + 1));
						productName = sandwichList.get(random.nextInt(sandwichList.size()));
						productInfo = sandwichMap.get(productName);
					}
					case 3 -> {
						dto.setCategoryMain("dessert");
						dto.setCategorySub("dessert" + (random.nextInt(3) + 1));
						productName = dessertList.get(random.nextInt(dessertList.size()));
						productInfo = dessertMap.get(productName);
					}
				}

				// 상품명
				dto.setProductName(productName);

				// 상품 상세 정보 (섬머노트 HTML)
				dto.setProductInfo(productInfo);

				// 가격 (랜덤 2,000 ~ 20,000원)
				int price = (random.nextInt(10) + 2) * 1000;
				dto.setPrice(price);

				// 정기배송 여부 랜덤 (0 or 1)
				dto.setIsSubscription(random.nextInt(2));

				// 판매자/상점
				dto.setMemberNo(memberNo);
				dto.setShopNo(shopNo);

				// 썸네일 (고정)
				dto.setThumbnailName("default.png");
				dto.setThumbnailPath("/upload/product/");

				// 한줄소개
				dto.setProductSummary(productName + " 맛있게 즐겨보세요!");

				// 알레르기 정보 (랜덤 선택)
				String[] allergyArr = { "밀", "우유", "계란", "견과류", "없음" };
				dto.setAllergyInfo(allergyArr[random.nextInt(allergyArr.length)]);

				// 영양 성분 (랜덤 예시)
				dto.setNutritionInfo("열량: " + (100 + random.nextInt(300)) + "kcal, 탄수화물: "
						+ (10 + random.nextInt(50)) + "g, 단백질: "
						+ (1 + random.nextInt(10)) + "g, 지방: "
						+ (1 + random.nextInt(20)) + "g");

				// DB 저장
				productMapper.insertProduct(dto);
			}
		}
	}
	
	//목록 조회 시 카드에 출력시킬 값 저장
	public void setCardInfo(ArrayList<ProductDTO> list) {
		for(ProductDTO dto : list) { //상품 꺼내기			
			//리뷰 개수 넣기
			int count = productReviewMapper.selectReviewTotal(dto.getProductNo()); //해당 상품의 리뷰 개수 조회
			dto.setReviewCount(count); //각 상품에 개수 set
			
			//상점명 넣기
			String shopName = productMapper.getShop(dto.getShopNo()).getShopName(); //상점명 리턴
			dto.setShopName(shopName); //상품에 shopName저장
						
			//상점 영업 상태 넣기
			int status = 0; //변수 초기화
			LocalTime open = productMapper.getShop(dto.getShopNo()).getOpenTime(); //여는 시간
			LocalTime close = productMapper.getShop(dto.getShopNo()).getCloseTime(); //닫는 시간
			if((LocalTime.now()).isBefore(open) || (LocalTime.now()).isAfter(close)){
				//현재 시간이 영업 전이거나 종료 후라면
				status=1; //영업 상태에 1 대입
			}
			dto.setShopStatus(status); //각 상품에 상태 set
		}
	}	
	
	// 인기 상품 목록 10개 (메인)
	public ArrayList<ProductDTO> popularList() {
		//목록 조회
		ArrayList<ProductDTO> list = productMapper.getPopularList();
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}
	
	//시군구 인기상품 목록 10개
	public ArrayList<ProductDTO> sigunguPopularList(String sigungu){
		//목록 조회
		ArrayList<ProductDTO> list = productMapper.getSigunguPopularList(sigungu);
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}

	// 최신 상품 목록 10개 (메인)
	public ArrayList<ProductDTO> newList() {
		//목록 조회
		ArrayList<ProductDTO> list = productMapper.getNewList();
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);

		return list;
	}
	
	// 시군구별 최신 상품 목록 10개 (메인)
	public ArrayList<ProductDTO> sigunguNewList(String sigungu) {
		ArrayList<ProductDTO> list = productMapper.getSigunguNewList(sigungu);
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}	

	//카테고리 이름
	public String categoryName(String cate) {
		return productMapper.getCategoryName(cate);
	}
	
	//카테고리 코드
	public String categoryCode(String cate) {
		return productMapper.getCategoryCode(cate);
	}
	
	// 전체 상품 목록
	public ArrayList<ProductDTO> list() {
		ArrayList<ProductDTO> list = productMapper.getList();
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}
	
	// 시군구별 전체 상품 목록
	public ArrayList<ProductDTO> sigunguList(String sigungu) {
		ArrayList<ProductDTO> list = productMapper.getSigunguList(sigungu);
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}
	
	//정렬 시군구별 전체 상품 목록
	public ArrayList<ProductDTO> sortSigunguList(String sigungu, String sortType){
		ArrayList<ProductDTO> list = new ArrayList<>();		
		// 모든 상품 sortType에 따라
		switch(sortType){
		case "sales" : // 구매순 정렬
			list = productMapper.bySalesSigunguList(sigungu);
			break;
		case "new" : // 최신순 정렬
			list = productMapper.getSigunguList(sigungu);
			break;
		case "rating" : // 별점순 정렬
			list = productMapper.byRatingSigunguList(sigungu);
			break;
		case "price" : // 낮은 가격순 정렬
			list = productMapper.byPriceSigunguList(sigungu);
			break;
		case "priceDesc" : // 높은 가격순 정렬
			list = productMapper.byPriceDescSigunguList(sigungu); 
			break;
		}
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}

	// 동도로별 전체 상품 목록
	//public ArrayList<ProductDTO> bnameList(String sigungu, String bname) {
	//	ArrayList<ProductDTO> list = productMapper.getBnameList(sigungu, bname);
	//	//상품 카드에 필요한 값 저장
	//	this.setCardInfo(list);
	//	return list;
	//}

	// 카테고리 상품 목록
	public ArrayList<ProductDTO> cateList(String category) {
		ArrayList<ProductDTO> list = productMapper.getCateList(category);
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}
	
	// 카테고리 서브 목록
//	public ArrayList<ProductDTO> getSubCateList(String sub) {
//		ArrayList<ProductDTO> list = productMapper.getCateList(sub);
		//상품 카드에 필요한 값 저장
//		this.setCardInfo(list);
//		return list;
//	}	

	// 시군구별 카테고리 상품 목록
	public ArrayList<ProductDTO> sigunguCateList(String category, String sigungu) {
		ArrayList<ProductDTO> list = productMapper.getSigunguCateList(category, sigungu);
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}	
	
	//동도로별 카테고리 상품 목록
	//public ArrayList<ProductDTO> bnameCateList(String category, String sigungu, String bname) {
	//	ArrayList<ProductDTO> list = productMapper.getBnameCateList(category, sigungu, bname);
		//상품 카드에 필요한 값 저장
	//	this.setCardInfo(list);
	//	return list;
	//}		

	//정렬 - 시군구별 카테고리 목록
	public ArrayList<ProductDTO> sortSigunguCateList (String category, String sigungu, String sortType) {
		ArrayList<ProductDTO> list = new ArrayList<>();		
		// 모든 상품 sortType에 따라
		switch(sortType){
		case "sales" : // 구매순 정렬
			list = productMapper.bySalesSigunguCateList(category, sigungu);
			break;
		case "new" : // 최신순 정렬
			list = productMapper.getSigunguCateList(category, sigungu);
			break;
		case "rating" : // 별점순 정렬
			list = productMapper.byRatingSigunguCateList(category, sigungu);
			break;
		case "price" : // 낮은 가격순 정렬
			list = productMapper.byPriceSigunguCateList(category, sigungu);
			break;
		case "priceDesc" : // 높은 가격순 정렬
			list = productMapper.byPriceDescSigunguCateList(category, sigungu); 
			break;
		}		
		//상품 카드에 필요한 값 저장
		this.setCardInfo(list);
		return list;
	}	
	
	// 상품 상세 조회
	public ProductDTO product(int productNo) {
	    ProductDTO dto = productMapper.getProduct(productNo);

	    if (dto == null) {
	        // 상품이 없으면 예외 처리
	        throw new RuntimeException("해당 상품이 존재하지 않습니다. productNo=" + productNo);
	        // 또는 return null; 후 Controller에서 처리
	    }

	    ShopDTO shop = productMapper.getShop(dto.getShopNo());
	    int status = 0;
	    if(shop != null) {
	        LocalTime open = shop.getOpenTime();
	        LocalTime close = shop.getCloseTime();
	        if (LocalTime.now().isBefore(open) || LocalTime.now().isAfter(close)) {
	            status = 1;
	        }
	    }
	    dto.setShopStatus(status);
	    return dto;
	}


	// @Query("""
	// SELECT s,
	// (6371 * acos(
	// cos(radians(:lat)) * cos(radians(s.latitude)) *
	// cos(radians(s.longitude) - radians(:lon)) +
	// sin(radians(:lat)) * sin(radians(s.latitude))
	// )) AS distance
	// FROM Shop s
	// WHERE s.shopSido = :sido
	// ORDER BY distance ASC
	// """)
	// List<Object[]> findNearbyShops(
	// @Param("lat") double lat,
	// @Param("lon") double lon,
	// @Param("sido") String userSido);

	// shopNo 기준 상품 조회
	public List<ProductDTO> getProductsByShop(int shopNo) {
		List<ProductDTO> list = productMapper.getProductsByShop(shopNo);
		return list != null ? list : new ArrayList<>();
	}

	// shopNo 으로 shopName을 가지고 옴
	public String getShopName(int shopNo) {
		ShopDTO shop = productMapper.getShop(shopNo);
		return shop.getShopName();
	};

	// ===== 헤더 검색창 (작업자 : 안성진)=====
	// 검색 기능
	// 키워드로 상품 검색 (상품명, 상점명)
	public List<ProductDTO> searchProducts(String keyword) {
		// Mapper 를 호출하여 DB에서 검색 결과 가져오기
		List<ProductDTO> list = productMapper.searchProducts(keyword);
		// null 방지 : 검색 결과가 없으면 빈 리스트 반환
		return list != null ? list : new ArrayList<>();
	}

	// 작업자 : 안성진
	// 검색 기능
	// 연관 검색어 조회 (검색어와 관련된 상품명을 최대 5개 가져옴)
	public List<String> getRelatedKeywords(String keyword) {
		// Mapper에서 연관 검색어 조회
		List<String> list = productMapper.getRelatedKeywords(keyword);
		// null 방지 : 결과 없으면 빈 리스트 반환
		return list != null ? list : new ArrayList<>();
	}

	// 작업자 : 안성진
	// 상품 번호로 상점 정보 조회
	public ShopDTO getShop(int shopNo) {
		return productMapper.getShop(shopNo);
	}
	
	// 작업자 : 안성진
	// 상품 번호로 상품정보 가져오기
	public ProductDTO getProductByProductNo(int productNo) {
		// TODO Auto-generated method stub
		ProductDTO product = productMapper.getProductByProductNo(productNo);
		return product;
	}
	// ===== 헤더 검색창 종료 =====

	// ===== 상품 상세 정보 찜 관련 (작업자 : 안성진) =====
	// 찜 기능
	// memberNo, productNo
	@Transactional(readOnly = true)
	public boolean isWished(int memberNo, int productNo) {
		return wishMapper.exists(memberNo, productNo) > 0;
	}

	// 작업자 : 안성진
	// 찜 토글
	// memberNo, productNo
	@Transactional
	public boolean toggleWish(int memberNo, int productNo) {
		if (wishMapper.exists(memberNo, productNo) > 0) {
			// 이미 찜한 상태 -> 삭제
			wishMapper.deleteWish(memberNo, productNo);
			return false;
		} else {
			// 찜하지 않은 상태 -> 추가
			WishDTO dto = new WishDTO();
			dto.setMemberNo(memberNo);
			dto.setProductNo(productNo);
			wishMapper.insertWish(dto);
			return true;
		}
	}

	// 작업자 : 안성진
	// 특정 상품의 총 찜 수
	// productNo
	@Transactional(readOnly = true)
	public int wishCount(int productNo) {
		return wishMapper.countByProduct(productNo);
	}
	// ===== 상품 상세 정보 찜 관련 종료 =====

	// ===== 회원이 판매자에게 문의 =====
	// 작업자 : 안성진
	// 상품 번호로 상점 번호 (shopNo) 조회
	// QnA 등록 시 productNo만 넘어오는 경우, 위임해서 shopNo를 얻음
	@Transactional(readOnly = true)
	public Integer getShopNoByProductNo(int productNo) {
		ProductDTO p = productMapper.getProductByProductNo(productNo);
		return (p != null) ? p.getShopNo() : null;
	}
	// ===== 회원이 판매자에게 문의 종료 =====
	
	//	==== 작업자 : 맹재희 
	//	=== 상품 수정 및 삭제 ====
	
	public ProductDTO getProductById(int productNo) {
	    return getProductByProductNo(productNo);
	}

	public int update(ProductDTO product) {
	    return productMapper.updateProduct(product); // Mapper에서 <update> 쿼리 필요
	}

	public void deleteProduct(int productNo) {
	    productMapper.deleteProduct(productNo); // MyBatis Mapper 호출
	}
// 	========== 상품 수정 및 삭제  끝 ================



	

}
