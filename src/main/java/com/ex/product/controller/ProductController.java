package com.ex.product.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ex.main.model.data.CategoryDTO;
import com.ex.main.model.service.MainService;
import com.ex.member.model.data.MemberDTO;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.data.ProductFileDTO;
import com.ex.product.model.data.ProductInsertForm;
import com.ex.product.model.data.ProductReviewDTO;
import com.ex.product.model.repository.ProductReviewMapper;
import com.ex.product.model.service.ProductReviewService;
import com.ex.product.model.service.ProductService;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.ShopRepository;
import com.ex.shop.model.service.ShopService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product/*")
public class ProductController {
	private final ProductService productService;
	private final ShopRepository shopRepository;
	private final ShopService shopService;
	private final MainService mainService;
	private final ProductReviewService reviewService;
	private final ProductReviewMapper productReviewMapper;

	// 상품 등록 폼: http://localhost:8080/product/insert
	// @PreAuthorize("isAuthenticated()") //로그인 검증 - 비로그인시 로그인페이지로
	@GetMapping("insert") // 필요한 값: 로그인 session(MemberDTO) - memberNo, shopNo
	public String insertForm(ProductInsertForm productInsertForm, HttpSession session, HttpServletRequest request,
			Model model) { // HttpServletRequest 이전 페이지 주소 찾기에 사용
		int memberNo = 0; // memberNo 초기화
		int shopNo = 0; // shopNo 초기화

		// 로그인 여부 검사 (*role=1이면 접근 막기)
		if (session.getAttribute("user") == null) { // 로그인하지 않았다면
			// 시큐리티 처리 후 삭제 예정
			model.addAttribute("title", "접근제한");
			model.addAttribute("msg", "로그인이 필요합니다.");
			model.addAttribute("icon", "error");
			model.addAttribute("loc", "/member/login");
			return "/common/msg";
		} else if (session != null && !(session.getAttribute("user").equals(null))) { // 로그인했고, user의 값을 가지고 있다면
			MemberDTO dto = (MemberDTO) session.getAttribute("user"); // session에서 값 꺼내기
			memberNo = dto.getMemberNo(); // session에서 memberNo 꺼내기
			// 상점을 가진 판매자인지 확인
			ShopDTO shop = shopService.getMyShopByMemberNo(memberNo);
			if (shop == null) {
				model.addAttribute("title", "접근제한");
				model.addAttribute("msg", "접근 권한이 없어 메인으로 이동합니다.");
				model.addAttribute("icon", "error");
				model.addAttribute("loc", "/");
				return "/common/msg";
			}
			shopNo = shop.getShopNo();// memberNo로 shopNo 꺼내기
		}

		// 폼객체에 shopNo, memberNo, productNo 저장하기
		productInsertForm.setShopNo(shopNo);
		productInsertForm.setMemberNo(memberNo);
		productInsertForm.setProductNo(productService.newProductNo());

		// 카테고리 조회 내용 뷰로 전달
		List<CategoryDTO> category = mainService.getCategory();
		model.addAttribute("category", category);

		String returnURL = request.getHeader("REFERER");// 상품 등록 폼 이전에 접속한 페이지의 주소
		session.setAttribute("returnURL", returnURL);// 세션에 저장

		return "/product/insertForm";
	}

	// 상품 등록 시 파일 업로드 처리
	@PostMapping("uploadFile")
	@ResponseBody // 결과를 바로 요청의 응답으로 전송
	public ResponseEntity<?> ajaxUploadFile(@RequestParam("files") MultipartFile[] files,
			@RequestParam("productNo") int productNo, ProductInsertForm productInsertForm, Model model) {
		// 업로드된 파일을 담은 files, 상품번호, insertForm에 thumbnailName저장

		// formData에 들어있는 데이터 @RequestParam("데이터명")으로 꺼내기
		int result = 0; // 업로드 성공 여부 판단용

		// 1.파일 업로드
		for (MultipartFile f : files) { // 받은 파일 꺼내기
			// 파일 이름 중복 방지
			String newName = UUID.randomUUID().toString().replace("-", "") + f.getOriginalFilename(); // UUID로 생성한 "-"뺀
																										// 난수 + 원래 파일명

			// 파일 업로드 경로 지정;
			// //뒤에 파일명이 와야하므로 \\까지 넣어주기
			String filePath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\upload\\product\\";

			// 업로드 처리
			File file = new File(filePath + newName); // 지정된 경로 + 새 파일명의 파일객체 생성
			if (f.getContentType().split("/")[0].equals("image")) { // 파일 유형이 image인 것만
				try {
					f.transferTo(file); // 멀티파트파일 객체에서 file로 파일 복사(업로드)
				} catch (Exception e) { // 업로드 실패
					e.printStackTrace();
					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패"); // 500에러 반환
				}
			} // 업로드 종료

			// 2. 업로드한 파일 productFile에 insert
			ProductFileDTO dto = new ProductFileDTO();
			dto.setProductNo(productNo);
			dto.setFileName(newName);
			dto.setFilePath(filePath);
			result = productService.insertFile(dto);
			if (result != 1) { // insert 실패 시
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("DB저장 실패");
			}
		} // for문 종료

		// 업로드된 파일(명)의 총 개수 리턴
		int countFiles = (productService.getFileNames(productNo)).size(); // 상품번호에 해당하는 파일명들.의 갯수
		return ResponseEntity.ok(countFiles); // ajax처리 완료 시 업로드된 파일 갯수 리턴
	}

	// 썸머노트 이미지 업로드 경로 설정
	@PostMapping("summernoteImg")
	@ResponseBody
	public ResponseEntity<String> ajaxSummernoteUpload(@RequestParam("files") MultipartFile[] files) {

		try {// 파일 자동 보이는거 되면 해보기
			String uploadPath = System.getProperty("user.dir")
					+ "\\src\\main\\resources\\static\\upload\\productInfo//";
			// "D:\\YOON\\spring\\Final_Project\\finalProject\\src\\main\\resources\\static\\upload\\productInfo//";
			// //서버 내 주소
			String uploadName = "";
			for (MultipartFile file : files) {
				uploadName = UUID.randomUUID().toString().replace("-", "") + file.getOriginalFilename(); // UUID로 생성한
																											// "-"뺀 난수 +
																											// 원래 파일명
				File f = new File(uploadPath + uploadName); // 지정된 파일경로+파일명의 파일 객체 생성
				file.transferTo(f); // 멀티파트에서 파일객체로 데이터 복사(업로드)
			}
			String url = uploadName;
			return ResponseEntity.ok().body(url);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("summernote업로드 실패");
		}

	}

	// 상품 등록 처리
	// @PreAuthorize("isAuthenticated()") //로그인 검증 - 비로그인시 로그인페이지로
	@PostMapping("insert") // 필요한 값: productInsertForm, returnUrl-상품 등록 페이지 접속하기 이전의 경로
	public String insertFormPro(@Valid ProductInsertForm productInsertForm, BindingResult bindingResult, Model model,
			HttpSession session, RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) { // 유효성검사 결과에 에러가 있다면
			// 카테고리 조회 내용 뷰로 전달
			List<CategoryDTO> category = mainService.getCategory();
			model.addAttribute("category", category);
			return "/product/insertForm"; // 폼으로 돌아가기
		}

		// 썸네일명 가져오기
		int productNo = productInsertForm.getProductNo(); // insert에 사용된 productNo
		String thumbnailName = (productService.getFileNames(productNo)).getFirst(); // 업로드된 파일 중 첫번째 파일명

		// 상품 폼 insert
		// insertForm에서 값 꺼내 dto에 set
		ProductDTO dto = new ProductDTO();
		dto.setProductNo(productInsertForm.getProductNo());
		dto.setProductName(productInsertForm.getProductName());
		dto.setPrice(productInsertForm.getPrice());
		if (productInsertForm.getIsSubscription() == null) {
			dto.setIsSubscription(0);
		} else {
			dto.setIsSubscription(productInsertForm.getIsSubscription());
		}
		dto.setMemberNo(productInsertForm.getMemberNo());
		dto.setShopNo(productInsertForm.getShopNo());
		dto.setThumbnailName(thumbnailName); // 첫 번째 파일명
		dto.setThumbnailPath("/upload/product/"); // 서버 내 업로드된 파일들의 경로(고정)
		dto.setCategoryMain(productInsertForm.getCategoryMain());
		dto.setCategorySub(productInsertForm.getCategorySub());
		dto.setProductInfo(productInsertForm.getProductInfo());
		dto.setProductSummary(productInsertForm.getProductSummary());
		dto.setAllergyInfo(productInsertForm.getAllergyInfo());
		dto.setNutritionInfo(productInsertForm.getNutritionInfo());
		int result = productService.insert(dto);// 상품 insert하는 메서드
		if (result != 1) { // 상품 insert 실패 시
			return "/product/insertForm"; // 폼으로 돌아가기
		}
		/*
		 * else { // 상품 insert 성공 시
		 * //상품 옵션이 입력됐는지 확인
		 * List<ProductOptionForm> options = productInsertForm.getOptions(); //옵션 폼 리스트
		 * 꺼내기
		 * if ( options!=null ) { // 옵션 폼에 값이 존재할 경우
		 * // 상품 옵션 insert
		 * int result2 = 0;// 상품 옵션 insert결과 확인 용
		 * // options에서 옵션 폼(Map<옵션명, 값>)꺼내기
		 * for( ProductOptionForm option : options) {
		 * //productOption에 insert
		 * ProductOptionDTO oto = new ProductOptionDTO(); //옵션DTO 생성
		 * oto.setProductNo(productNo); //상품 번호
		 * oto.setOptionName(option.getName()); //상품명
		 * oto.setOptionPrice(option.getPrice()); //상품 가격
		 * result2 = productService.insertOption(oto); //상품 옵션 insert
		 * if(result2 != 1) { //상품 옵션 insert 실패 시
		 * return "/product/insertForm";
		 * }
		 * }//옵션 꺼내는 반복문 종료
		 * }
		 * }// 상품 정보, 옵션 insert 종료
		 */
		// 상품 등록 후 이전 페이지로 돌아가게 하기
		String returnURL = (String) session.getAttribute("returnURL"); // 세션에서 이전 페이지 주소 꺼내기
		session.removeAttribute("returnURL"); // 세션에서 이전 페이지의 주소 삭제
		if (returnURL == null || returnURL.isBlank()) { // returnURL이 비었거나 공백이라면,
			model.addAttribute("title", "페이지 이동");
			model.addAttribute("msg", "이전 페이지를 찾을 수 없어 메인으로 이동합니다.");
			model.addAttribute("icon", "error");
			model.addAttribute("loc", "/");
			return "/common/msg"; // 메인으로 이동하기 전 알림 띄우기
		}

		// 상품 등록 성공 시
		redirectAttributes.addFlashAttribute("insertSuccess", "등록이 완료되었습니다.");

		return "redirect:" + returnURL; // 상품 등록 후 이전 페이지로 이동
	}

	// 상품 목록: http://localhost:8080/product/list
	@GetMapping("list/{categoryCode}") // 맨처음 목록 페이지 들어갔을 때, 필요 매개변수: 카테고리 대분류명(String)
	public String list(@PathVariable("categoryCode") String cate, @RequestParam("sub") Optional<String> sub,
			HttpSession session, Model model) {

		// 로그인한 사용자라면 MemberNo 뷰로 전송
		MemberDTO member = (MemberDTO) session.getAttribute("user");
		if (member != null) { // 로그인한 사용자라면
			model.addAttribute("memberNo", member.getMemberNo()); // memberNo 뷰로 전달
		}

		// 뷰로 전달할 변수 미리 초기화
		ArrayList<ProductDTO> list = new ArrayList<>();
		// category값에 따른 상품 목록 출력
		if (cate.equals("all")) { // category가 전달되지 않았다면
			list = productService.list(); // 전체 상품 목록 출력
		} else {
			// 소분류 카테고리일 시 sub를 cate로 사용
			if (sub.isPresent()) {
				cate = sub.get();
				model.addAttribute("sub", true); // 소분류 목록임을 알려주는 값 전달
			}
			list = productService.cateList(cate); // 카테고리 목록 출력
		}

		// 출력용 상품마다 shopName저장
		for (ProductDTO dto : list) { // 상품꺼내기
			// 찜 여부 저장
			int isWished = 0; // 찜 아님
			if (member != null && productService.isWished(member.getMemberNo(), dto.getProductNo())) { // 로그인되어있고, 찜이
																										// 되어있다면
				isWished = 1; // 찜 되어있음
			}
			dto.setIsWished(isWished);
		}
		// 요청(카테고리)에 해당하는 상품 목록 뷰로 전달
		// model.addAttribute("list", list);

		// 전체 카테고리가 아니라면 카테고리 이름 전달
		if (!cate.equals("all")) {
			String category = productService.categoryName(cate);
			model.addAttribute("category", category);
		}

		// 시(도) 목록 전달
		List<String> sidoList = shopRepository.findDistinctSido();
		model.addAttribute("sidoList", sidoList);

		return "/product/list";
	}

	// 시군구별 (카테고리별) 상품 목록
	@GetMapping("sigunguList") // 지역구에 따라 분류한 목록 보여짐
	public String ajaxSigungu(@RequestParam("cateName") String cate, @RequestParam("sido") String sido,
			@RequestParam("sigungu") String sigungu, HttpSession session, Model model) {

		// 로그인한 사용자라면 MemberNo 뷰로 전송
		MemberDTO member = (MemberDTO) session.getAttribute("user");
		if (member != null) { // 로그인한 사용자라면
			model.addAttribute("memberNo", member.getMemberNo()); // memberNo 뷰로 전달
		}

		// category값에 따른 상품 목록 출력
		// 뷰로 전달할 변수 미리 초기화
		ArrayList<ProductDTO> list = new ArrayList<>();
		// cateName으로 cateCode찾아 다시 cate에 대입
		cate = productService.categoryCode(cate);
		if (cate == null) { // 해당하는 카테고리 코드가 없을 경우
			list = productService.sigunguList(sigungu); // 전체 상품 목록 출력
		} else {
			list = productService.sigunguCateList(cate, sigungu); // 카테고리 목록 출력
			// 전체 카테고리가 아니라면 카테고리 이름 전달
			String category = productService.categoryName(cate);
			model.addAttribute("category", category);
		}

		// 출력용 상품마다 shopName저장
		for (ProductDTO dto : list) { // 상품꺼내기
			// 찜 여부 저장
			int isWished = 0; // 찜 아님
			if (member != null && productService.isWished(member.getMemberNo(), dto.getProductNo())) { // 로그인되어있고, 찜이
																										// 되어있다면
				isWished = 1; // 찜 되어있음
			}
			dto.setIsWished(isWished);
		}
		// 요청(카테고리)에 해당하는 상품 목록 뷰로 전달
		model.addAttribute("list", list);

		// 시(도) 목록 전달
		List<String> sidoList = shopRepository.findDistinctSido();
		model.addAttribute("sidoList", sidoList);

		return "product/sigunguList :: sigunguList";
	}

	// 정렬 - 시군구별 카테고리 목록
	@GetMapping("sigunguListSortBy")
	public String ajaxSortBy(@RequestParam("cateName") String cate, @RequestParam("sido") String sido,
			@RequestParam("sigungu") String sigungu, @RequestParam("sortType") String sortType, HttpSession session,
			Model model) {

		// 로그인한 사용자라면 MemberNo 뷰로 전송
		MemberDTO member = (MemberDTO) session.getAttribute("user");
		if (member != null) { // 로그인한 사용자라면
			model.addAttribute("memberNo", member.getMemberNo()); // memberNo 뷰로 전달
		}

		// 뷰로 전달할 변수 미리 초기화
		ArrayList<ProductDTO> list = new ArrayList<>();
		// cateName으로 cateCode찾아 다시 cate에 대입
		cate = productService.categoryCode(cate);
		// sortType에 따라 list 조회
		if (cate == null) { // 해당하는 카테고리 코드가 없을 경우
			list = productService.sortSigunguList(sigungu, sortType);
		} else { // 카테고리가 있는 경우
			list = productService.sortSigunguCateList(cate, sigungu, sortType);
			String category = productService.categoryName(cate);
			model.addAttribute("category", category);
		}

		// 출력용 상품마다 shopName저장
		for (ProductDTO dto : list) { // 상품꺼내기
			// 찜 여부 저장
			int isWished = 0; // 찜 아님
			if (member != null && productService.isWished(member.getMemberNo(), dto.getProductNo())) { // 로그인되어있고, 찜이
																										// 되어있다면
				isWished = 1; // 찜 되어있음
			}
			dto.setIsWished(isWished);
		}
		// 요청(카테고리)에 해당하는 상품 목록 뷰로 전달
		model.addAttribute("list", list);

		// 시(도) 목록 전달
		List<String> sidoList = shopRepository.findDistinctSido();
		model.addAttribute("sidoList", sidoList);

		return "product/sigunguList :: sigunguList";
	}

	// 상품 상세 조회: http://localhost:8080/product/detail/상품번호
	@GetMapping("detail/{num}") // 뷰로 전달: 상품번호에 해당하는 상품, 장바구니 폼

	public String detail(@PathVariable("num") int productNo, Model model, HttpSession session,
			HttpServletRequest request) {
		// 이전 페이지 전송
		String returnURL = request.getHeader("REFERER");// 상품 등록 폼 이전에 접속한 페이지의 주소
		if (returnURL != null && !returnURL.contains("update")) {
			session.setAttribute("returnURL", returnURL);
		}

		// Product 조회
		ProductDTO product = productService.product(productNo); // 상품 정보 리턴
		// ProductOption 조회
		// ArrayList<ProductOptionDTO> options = productService.getOptions(productNo);
		// //상품 옵션 리턴
		// if(options.size()>0) { //옵션이 있을 때만
		// model.addAttribute("options",options); // 상품 옵션 전달
		// }

		// shopName저장
		String shopName = productService.getShopName(product.getShopNo()); // 상점명 리턴
		product.setShopName(shopName); // 상품에 shopName저장
		// 상품 파일명(사진)리스트 리턴
		ArrayList<String> fileNames = productService.getFileNames(productNo);

		// 로그인한 사용자라면 MemberNo 뷰로 전송
		MemberDTO member = (MemberDTO) session.getAttribute("user");
		if (member != null) { // 로그인한 사용자라면
			model.addAttribute("memberNo", member.getMemberNo()); // memberNo 뷰로 전달
		}

		// 찜 상태/개수 내려주기
		boolean wished = false;
		int wishCount = productService.wishCount(productNo);
		if (member != null) {
			wished = productService.isWished(member.getMemberNo(), productNo);
		}

		model.addAttribute("wished", wished);
		model.addAttribute("wishCount", wishCount);

		// 해당 상품이 정기 배송 상품이라면
		// if(product.getIsSubscription()==1) {
		// model.addAttribute("isSubs",true);
		// }

		model.addAttribute("detail", product); // 상품 정보 뷰로 전달
		model.addAttribute("images", fileNames); // 상품 파일명(사진)리스트 뷰로 전달
		List<ProductReviewDTO> reviewList = productReviewMapper.selectProductReviewWithComments(productNo);

		model.addAttribute("reviewList", reviewList);
		return "/product/detail";
	}

	// 작업자 : 안성진
	// 검색 기능
	// 메인 페이지 검색 처리
	@GetMapping("search")
	public String searchProducts(@RequestParam("keyword") String keyword, // 사용자 입력 검색어
			Model model, HttpSession session) {

		MemberDTO member = (MemberDTO) session.getAttribute("user");
		if (member != null) {
			model.addAttribute("memberNo", member.getMemberNo());
		}

		// 검색어 공백 체크
		// 사용자가 아무것도 입력하지 않았을 경우, 빈 결과와 빈 연관 검색어 반환
		if (keyword == null || keyword.trim().isEmpty()) {
			model.addAttribute("list", new ArrayList<ProductDTO>()); // 빈 상품 리스트
			model.addAttribute("relatedKeywords", new ArrayList<>()); // 빈 연관 검색어 리스트
			model.addAttribute("keyword", ""); // 빈 검색어 표시
			return "/product/search";
		}

		// 1. 키워드로 상품 검색 결과 가져오기
		// 상품명과 상점명을 기준으로 검색
		List<ProductDTO> searchList = productService.searchProducts(keyword);

		// shopStatus = 0 인 상품만 나오도록 (정상 운영 중인 가게의 상품임)
		searchList = searchList.stream().filter(dto -> dto.getShopStatus() == 0).collect(Collectors.toList());

		// 2. 연관 검색어 가져오기
		// 검색어와 관련된 상품명 최대 5개 가져오기
		List<String> relatedKeywords = productService.getRelatedKeywords(keyword);

		// 3. 각 상품에 상점명 + 찜 여부 세팅
		if (searchList != null) {
			for (ProductDTO dto : searchList) {
				// 상점명
				String shopName = productService.getShopName(dto.getShopNo());
				dto.setShopName(shopName);

				// 로그인 상태라면 찜 여부 세팅
				if (member != null) {
					int isWished = productService.isWished(member.getMemberNo(), dto.getProductNo()) ? 1 : 0;
					dto.setIsWished(isWished);
				}
			}
		}

		// 4. 모델에 결과 전달
		model.addAttribute("list", searchList); // 검색된 상품 리스트
		model.addAttribute("relatedKeywords", relatedKeywords); // 연관 검색어 리스트
		model.addAttribute("keyword", keyword); // 검색어도 뷰에서 표시 가능

		// 5. 검색 결과 페이지 return
		return "/product/search";
	}

	// 작업자 : 안성진
	// 찜 토글 (로그인 필요) 매개변수: productNo
	@PostMapping("wish/toggle")
	@ResponseBody
	public ResponseEntity<?> toggleWish(@SessionAttribute(name = "user", required = false) MemberDTO user,
			@RequestBody java.util.Map<String, Integer> body) {
		if (user == null) {
			return ResponseEntity.status(401).body(java.util.Map.of("status", "login"));
		}

		Integer productNo = body.get("productNo");

		if (productNo == null) {
			return ResponseEntity.badRequest().body(java.util.Map.of("status", "error", "msg", "productNo required"));
		}

		boolean wished = productService.toggleWish(user.getMemberNo(), productNo);
		int count = productService.wishCount(productNo);

		return ResponseEntity.ok(java.util.Map.of(
				"status", "ok",
				"wished", wished,
				"count", count));
	}

	// 작업자 : 맹재희 >> 윤예솔
	// == 상품 수정 및 삭제 ==
	// 상품 수정 폼
	@GetMapping("update/{num}")
	public String showUpdateForm(@PathVariable("num") int productNo, Model model, HttpSession session,
			ProductInsertForm productInsertForm, HttpServletRequest request) {
		int memberNo = 0; // memberNo 초기화
		int shopNo = 0; // shopNo 초기화
		// 상품정보 가져오기
		ProductDTO product = productService.getProductByProductNo(productNo);

		// 로그인 여부 검사 (*role=1&작성자가 아니면 접근 막기)
		if (session.getAttribute("user") == null) { // 로그인하지 않았다면
			model.addAttribute("title", "접근제한");
			model.addAttribute("msg", "로그인이 필요합니다.");
			model.addAttribute("icon", "error");
			model.addAttribute("loc", "/member/login");
			return "/common/msg"; // 로그인페이지로 이동시키기
		} else if (session != null && !(session.getAttribute("user").equals(null))) { // 로그인했고, user의 값을 가지고 있다면
			MemberDTO dto = (MemberDTO) session.getAttribute("user"); // session에서 값 꺼내기
			memberNo = dto.getMemberNo(); // session에서 memberNo 꺼내기
			// 상점을 가진 판매자인지 확인
			shopNo = shopService.getMyShopByMemberNo(memberNo).getShopNo();
			// 이 글의 작성자와 일치하지 않는다면
			if (product.getShopNo() != shopNo) {
				model.addAttribute("title", "접근제한");
				model.addAttribute("msg", "접근 권한이 없어 메인으로 이동합니다.");
				model.addAttribute("icon", "error");
				model.addAttribute("loc", "/");
				return "/common/msg"; // 메인으로 이동시키기
			}
		}

		// 폼객체에 기존정보 저장하기
		productInsertForm.setShopNo(shopNo);
		productInsertForm.setMemberNo(memberNo);
		productInsertForm.setProductNo(product.getProductNo());
		productInsertForm.setProductName(product.getProductName());
		productInsertForm.setPrice(product.getPrice());
		productInsertForm.setIsSubscription(product.getIsSubscription());
		productInsertForm.setMemberNo(product.getMemberNo());
		productInsertForm.setCategoryMain(product.getCategoryMain());
		productInsertForm.setCategorySub(product.getCategorySub());
		productInsertForm.setProductInfo(product.getProductInfo());
		productInsertForm.setProductSummary(product.getProductSummary());
		productInsertForm.setAllergyInfo(product.getAllergyInfo());
		productInsertForm.setNutritionInfo(product.getNutritionInfo());
		// 업로드된 상품 사진은 보여주기만 하기(수정처리 x)
		List<String> fileNames = productService.getFileNames(productNo);
		model.addAttribute("fileNames", fileNames);

		// 카테고리 조회 내용 뷰로 전달
		List<CategoryDTO> category = mainService.getCategory();
		model.addAttribute("category", category);

		String returnURL = request.getHeader("REFERER");// 상품 등록 폼 이전에 접속한 페이지의 주소
		session.setAttribute("returnURL", returnURL);// 세션에 저장

		/*
		 * ProductDTO product = productService.getProductById(productNo);
		 * 
		 * if (product == null) {
		 * return "redirect:/product/list";
		 * }
		 * 
		 * model.addAttribute("product", product);
		 */

		return "/product/update"; // 수정 화면
	}

	// 작업자 : 맹재희 >>윤예솔
	// 상품 수정 처리
	@PostMapping("update")

	public String updateProduct(@Valid ProductInsertForm productInsertForm, BindingResult bindingResult, Model model,
			HttpServletRequest request, RedirectAttributes redirectAttributes) { //

		if (bindingResult.hasErrors()) { // 유효성검사 결과에 에러가 있다면
			// 카테고리 조회 내용 뷰로 전달
			List<CategoryDTO> category = mainService.getCategory();
			model.addAttribute("category", category);
			// 업로드된 상품 사진은 보여주기만 하기(수정처리 x)
			List<String> fileNames = productService.getFileNames(productInsertForm.getProductNo());
			model.addAttribute("fileNames", fileNames);

			return "/product/update"; // 폼으로 돌아가기
		}

		// 상품 폼 update
		// insertForm에서 값 꺼내 dto에 set
		ProductDTO dto = new ProductDTO();
		dto.setProductNo(productInsertForm.getProductNo());
		dto.setProductName(productInsertForm.getProductName());
		dto.setPrice(productInsertForm.getPrice());
		if (productInsertForm.getIsSubscription() == null) {
			dto.setIsSubscription(0);
		} else {
			dto.setIsSubscription(productInsertForm.getIsSubscription());
		}
		dto.setMemberNo(productInsertForm.getMemberNo());
		dto.setShopNo(productInsertForm.getShopNo());
		dto.setCategoryMain(productInsertForm.getCategoryMain());
		dto.setCategorySub(productInsertForm.getCategorySub());
		dto.setProductInfo(productInsertForm.getProductInfo());
		dto.setProductSummary(productInsertForm.getProductSummary());
		dto.setAllergyInfo(productInsertForm.getAllergyInfo());
		dto.setNutritionInfo(productInsertForm.getNutritionInfo());
		int result = productService.update(dto);// 상품 update하는 메서드
		if (result != 1) { // 상품 update 실패 시
			// 업로드된 상품 사진은 보여주기만 하기(수정처리 x)
			List<String> fileNames = productService.getFileNames(productInsertForm.getProductNo());
			model.addAttribute("fileNames", fileNames);
			return "/product/update"; // 폼으로 돌아가기
		}

		/*
		 * ProductDTO existing = productService.getProductById(product.getProductNo());
		 * if (existing == null) {
		 * redirectAttributes.addFlashAttribute("msg", "해당 상품이 존재하지 않습니다.");
		 * return "redirect:/";
		 * }
		 * // 상품 업데이트
		 * productService.updateProduct(product);
		 */
		// 상품 수정 성공 시
		redirectAttributes.addFlashAttribute("updateSuccess", "수정이 완료되었습니다.");

		return "redirect:/product/detail/" + productInsertForm.getProductNo();
	}

	// 작업자 : 맹재희
	// 상품 삭제
	@GetMapping("delete/{num}")
	public String deleteProduct(@PathVariable("num") int productNo, RedirectAttributes redirectAttributes) {
		ProductDTO existing = productService.getProductById(productNo);

		if (existing == null) {
			redirectAttributes.addFlashAttribute("msg", "해당 상품이 존재하지 않습니다.");
			return "redirect:/";
		}

		// 상품 삭제
		productService.deleteProduct(productNo);
		redirectAttributes.addFlashAttribute("msg", "상품이 삭제되었습니다.");
		return "redirect:/shop/shopmain";
	}

	// 작업자 : 맹재희
	// 상품 삭제 저장
	@PostMapping("delete/{num}")
	public String deletesaveProduct(@PathVariable("num") int productNo, RedirectAttributes redirectAttributes) {
		ProductDTO existing = productService.getProductById(productNo);

		if (existing == null) {
			redirectAttributes.addFlashAttribute("msg", "해당 상품이 존재하지 않습니다.");
			return "redirect:/shop/shopmain";
		}

		productService.deleteProduct(productNo);
		redirectAttributes.addFlashAttribute("msg", "상품이 삭제되었습니다.");
		return "redirect:/shop/shopmain";
	}

}
