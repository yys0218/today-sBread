package com.ex.order.controller;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ex.TestController;
import com.ex.member.model.data.AddressBookDTO;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.member.model.service.AddressBookService;
import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderForm;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.data.OrderTimeDTO;
import com.ex.order.model.data.ShoppingCartDTO;
import com.ex.order.model.data.ShoppingCartForm;
import com.ex.order.model.repository.OrderDetailRepository;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.repository.ShoppingCartRepository;
import com.ex.order.model.service.PortOneService;
import com.ex.order.model.service.ShoppingCartService;
import com.ex.product.model.service.ProductService;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.ShopRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/order/*")
@RequiredArgsConstructor
public class OrderController {

    private final TestController testController; // 장바구니/결제 부분

	private final ShoppingCartService shoppingCartService;
	private final ShoppingCartRepository shoppingCartRepository;
	private final ShopRepository shopRepository;
	private final MemberRepository memberRepository;
	private final PortOneService portOneService;
	private final OrderHistoryRepository orderHistoryRepository;
	private final ProductService productService;
	private final OrderDetailRepository orderDetailRepository;
	private final AddressBookService addressBookService;

	// 주문 관련 더미데이터 추가
	// @GetMapping("dummyOrder")
	// public String dummyOrder() {
	// this.shoppingCartService.dummyOrder();
	// return "redirect:/";
	// }

	// 장바구니에 상품 추가
	@ResponseBody
	@PostMapping("cartInsert")
	// 시큐리티..ㅋㅋㅋㅋㅋ
	public ResponseEntity<String> ajaxInsertCart(@RequestBody ShoppingCartForm form) {
		// @RequestBody 요청에 담긴 JSON타입의 문자열을 자바 객체로 변환 >> gradle설정 후 form객체로 받기 성공!

		/* productNo : productno, shopNo : shopno, productName : productname, 
		 * price : price, quantity : 1, memberNo : memberno
		 */
		
		//로그인한 member객체 생성
		MemberDTO member = memberRepository.findByMemberNo(form.getMemberNo());
		if(member==null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("member조회 실패");
		}
		
		// 기존 상품인 경우, 기존 DB에 수량만 update
		Optional<ShoppingCartDTO> _inCart = shoppingCartRepository.findByProductNoAndMember(form.getProductNo(),member); // 추가할 상품 번호로 장바구니 조회
		ShoppingCartDTO inCart = null;
		if (_inCart.isPresent()) { // 기존 장바구니 레코드가 있다면
			inCart = _inCart.get();// 기존 장바구니 레코드 가져오기
			inCart.setQuantity(inCart.getQuantity() + form.getQuantity()); // 기존 수량에 입력받은 수량 더하기
			shoppingCartRepository.save(inCart); // 수량 업데이트
			return ResponseEntity.ok("장바구니에 기존 상품 수량 추가");
		}else {
			// 장바구니 테이블에 레코드 추가
			shoppingCartService.insertCart(form);			
		}
		
		return ResponseEntity.ok("장바구니에 새 상품 추가"); // 실패시 알아서 ajax에 error시의 응답이 실행됨 굿.
	}

	// 장바구니 접속: http://localhost:8080/order/cart
	@GetMapping("cart/{list}") //매개변수: 목록 종류
	// @PreAuthorize("isAuthenticated()") 시큐리티 설정 필요
	public String shoppingCart(@PathVariable("list") String list , HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		
		MemberDTO member = (MemberDTO) session.getAttribute("user");
		int memberNo = 0;
		if (!(member == null)) { // 로그인되어있다면
			memberNo = member.getMemberNo();
		} else { // 로그인되어있지않다면
			redirectAttributes.addFlashAttribute("showToast", "로그인이 필요합니다.");
			return "redirect:/member/login";
		}

		//어떤 상품 목록을 가져올 지 변수 초기화
		int isSubs =0; //일반 상품이면 0
		if(list.equals("subs")) { //정기 상품이면 1
			isSubs=1;
			model.addAttribute("isSubs",true); //정기상품목록 여부를 뷰에 전달
		}
		
		// 같은 상점별로 장바구니 내역이 구분된 list 뷰로 전송
		// shop을 key, 같은 shop을 가진 장바구니 목록을 value로 가진 Map 생성
		Map<ShopDTO, List<ShoppingCartDTO>> cartGroup = new HashMap<ShopDTO, List<ShoppingCartDTO>>();
		// 사용자 장바구니의 shop 조회
		List<ShopDTO> shopList = shoppingCartRepository.findByMemberGroupByShop(member);
		if(!(shopList.size()>0)){
		model.addAttribute("title", "경고");
            model.addAttribute("msg", "장바구니가 비어있습니다");
            model.addAttribute("icon", "warning");
            model.addAttribute("loc", "/");
            return "common/msg";}
		for (ShopDTO shop : shopList) {
			int isOpened = 0;//영업중
			//가게 영업여부 확인, 담기
			if(LocalTime.now().isBefore(shop.getOpenTime()) || LocalTime.now().isAfter(shop.getCloseTime())){
				//영업시간 전,후 라면
				isOpened = 1; //준비 중
			}
			shop.setIsOpened(isOpened); //shop에 대입
			// shop에 해당하는 장바구니 내역 조회
			List<ShoppingCartDTO> cartList = shoppingCartService.getCartList(shop, member, isSubs);
			for(ShoppingCartDTO cart : cartList) { //장바구니 내역 꺼내기
				//찜 여부 저장
				int isWished = 0; //찜 아님
				if( member!=null && productService.isWished(member.getMemberNo(), cart.getProductNo()) ) { //로그인되어있고, 찜이 되어있다면
					isWished = 1; //찜 되어있음
				}
				cart.setIsWished(isWished);					
			}
			if(cartList.size()>0) { //장바구니 내역이 존재한다면
				cartGroup.put(shop, cartList); // shop, 그에 해당하는 장바구니 내역 >>하나의 엔트리로 put
			} 
		}	
		model.addAttribute("cartGroup", cartGroup); // 엔트리<shop, cartList>의 S묶음 전달
		
		return "/order/shoppingCart";
	}
	
	
	// 장바구니에서 선택한 결과를 삭제
	@PostMapping("cartDelete")
	public String cartDelete(@RequestParam("cartNos") ArrayList<Integer> cartNos,
			RedirectAttributes redirectAttributes, HttpServletRequest request) {

		//버튼 클릭된 페이지로 돌려보내기
		String url =  request.getHeader("Referer");		

		// 입력받은 shoppingCart번호 삭제
		for (int cartNo : cartNos) { // cartNo꺼내기
			Optional<ShoppingCartDTO> _cart = shoppingCartRepository.findById(cartNo);// cart레코드 조회
			ShoppingCartDTO cart = null;
			if (_cart.isPresent()) {
				cart = _cart.get(); // cart에 대입
				shoppingCartRepository.delete(cart); // 해당 레코드 장바구니에서 삭제
				// 돌아간 페이지에서 처리완료 알림(toast)이 뜰 수 있도록
				redirectAttributes.addFlashAttribute("showToast", "삭제가 완료되었습니다."); // 리다이렉트 시 1회성으로 전송되는 k-v

			} else { //_cart가 비어있다면 (예외처리)
				//왔던 페이지로 돌아가 오류 알림
				redirectAttributes.addFlashAttribute("showError", "다시 시도해주세요.");
			}
		}
		
		//버튼 클릭된 페이지로 돌려보내기
		if(url != null) {
			return "redirect:"+url;
		} else {
			return "redirect:/order/cart/normal"; //기본 경로
		}
	}

	// 장바구니 내 모든 상품 삭제
	@PostMapping("cartDeleteAll")
	public String cartDeletetAll(HttpSession session, RedirectAttributes redirectAttributes, HttpServletRequest request) {

		// 로그인한 사용자 member 가져오기
		MemberDTO member = (MemberDTO) session.getAttribute("user");
		if (member == null) { // 로그인되어있지 않다면
			redirectAttributes.addFlashAttribute("showToast", "로그인이 필요합니다.");
			return "redirect:/member/login";
		}

		//버튼 클릭된 페이지로 돌려보내기
		String url =  request.getHeader("Referer");

		// member의 장바구니 가져오기
		ArrayList<ShoppingCartDTO> cartList = shoppingCartRepository.findByMember(member);
		for (ShoppingCartDTO cart : cartList) { // 장바구니 내역 1개씩 꺼내기
			shoppingCartRepository.delete(cart); // 장바구니 내역 삭제
		}

		// 돌아간 페이지에서 처리완료 알림(toast)이 뜰 수 있도록
		redirectAttributes.addFlashAttribute("showToast", "모든 상품이 삭제되었습니다."); // toast 메세지 전달

		//버튼 클릭된 페이지로 돌려보내기
		if(url != null) {
			return "redirect:"+url;
		} else {
			return "redirect:/order/cart/normal"; //기본 경로
		}
	}

	// 장바구니에서 선택한 결과를 주문 페이지로 제출
	@PostMapping("order") // 매개변수: 선택된 장바구니 번호리스트, 선택된 장바구니의 수량, orderForm
	// @PreAuthorize("isAuthenticated()") //로그인 여부 확인
	public String getOrder(@RequestParam("cartNos") ArrayList<Integer> cartNos, @RequestParam(name="isSubs", required=false) boolean isSubs,
			@RequestParam Map<String,String> quantities, HttpSession session, Model model, OrderForm orderForm, RedirectAttributes redirectAttributes) {
		// "quantities[]"와 같은 형태의 파라미터명은 SpringMVC가 자동으로 Map으로 매핑함
		// 선택된 상품의 값(quantity, option) update -> 선택된 cartNo만으로 다시 조회한 리스트 뷰로 전송

		// memberNo추출
		MemberDTO dto = (MemberDTO) session.getAttribute("user");
		int memberNo = 0;
		if (!(dto == null)) { // 로그인되어있다면
			memberNo = dto.getMemberNo();
			// 정기상품 결제라면
			if(isSubs) {
				model.addAttribute("isSubs",isSubs);
				//customerUid 전송
				//String customerUid = dto.getMemberId()+UUID.randomUUID().toString().replace("-","");
				//model.addAttribute("customerUid", customerUid);
			}
		} else { // 로그인되어있지않다면
			redirectAttributes.addFlashAttribute("showToast", "로그인이 필요합니다.");
			return "redirect:/member/login";
		}
		
		// 장바구니 수량 update
		for (Integer cartNo : cartNos) { // 선택된 cartNo 1개씩 뽑기
			//cartNo이 없는 quantity거르기
			Integer quantity = Integer.parseInt(quantities.get("quantities["+ cartNo +"]"));
			/*
			 * if(quantity == null) { //quantity가 대입되지 않았다면 continue; //반복문 처음으로 돌아가기 }
			 */
			shoppingCartService.updateCart(cartNo, quantity, memberNo); //해당되는 레코드의 quantity update
		}

		// 결제할 장바구니 내역 조회
		// memberNo에 해당하는 cartNo중에서 cartNos에 해당하는 레코드만 조회
		ArrayList<ShoppingCartDTO> selected = shoppingCartService.getSelectedList(memberNo, cartNos);
		// 결제할 장바구니 내역이 없으면(못찾았으면)
		if (selected.size() < 1) {
			return "redirect:/";
		}

		// 뷰로 전달
		model.addAttribute("list", selected); // 결제할 상품 내역

		// 총 결제 금액 계산
		int price = 0; // 상품 1개당 가격
		int quantity = 0; // 상품 개수
		int totalPrice = 0; // 총 금액
		for (ShoppingCartDTO cart : selected) {
			price = cart.getPrice();
			quantity = cart.getQuantity();
			totalPrice += (price * quantity); // 상품의 가격, 수량이 바뀔 때마다 더하고 대입되도록
		}

		// 배송비 가져오기
		ShopDTO shop = shopRepository.findByShopNo(selected.getFirst().getShop().getShopNo());
		// shop정보 리턴
		int deliveryFee = (int) shop.getDeliveryFee(); // shop레코드의 delivertyFee
		int orderPrice = deliveryFee + totalPrice;

		// orderForm에 주문 기본값(상품 정보, 주문 정보, 배송 정보) 세팅
		// 상품 정보
		orderForm.setShopNo(selected.getFirst().getShop().getShopNo()); // 상점 번호
		// orderForm.setCartNos(cartNos); //선택된 상품의 장바구니 번호들 (ArrayList<Integer>)
		orderForm.setTotalPrice(totalPrice); // 상품 총액
		orderForm.setDeliveryFee(deliveryFee); // 배송비
		orderForm.setOrderPrice(orderPrice); // 현재 주문의 결제 예정 금액
		// 구매자 정보/배송지 기본값
		orderForm.setBuyerNo(dto.getMemberNo()); // 구매자번호로 memberNo
		orderForm.setOrderName(dto.getMemberName()); // 주문자이름으로 memberName
		orderForm.setOrderPhone(dto.getMemberPhone()); // 주문자연락처로 memberPhone
		orderForm.setOrderAddress(dto.getMemberAddress()); // 배송지로 memberAddress
		
		//주문자의 주소록 전달
		List<AddressBookDTO> list = addressBookService.getAllAddresses(dto.getMemberId());
		//저장된 배송지가 없다면 기본배송지 추가
		if(list.size()==0) {
			addressBookService.createDefaultAddressFromSignup(dto);
			list.add(addressBookService.getMyOnlyAddress(dto));
		}
		model.addAttribute("addressList", list);
		
		
		return "/order/order";
	}

	// detail에서 바로 구매 클릭 시 주문 페이지로 이동
	@PostMapping("orderNow") // 매개변수: ajax로 받은 데이터(shoppingCartForm객체로 받기)
	@ResponseBody // redirectURL 반환
	public Map<String, Object> ajaxOrderNow(Model model, HttpSession session, @RequestBody ShoppingCartForm cartForm) {

		// return용 Map객체 생성
		Map<String, Object> response = new HashMap<>();

		// 구매자 Member객체 가져오기
		MemberDTO dto = memberRepository.findByMemberNo(cartForm.getMemberNo());
				// DB에서 가져오지 않으면 엔티티 내 컬럼에 저장할 수 없다.(영속성 컨텍스트여야함)
		// 로그인한 사용자인지 확인
		if (dto == null) { // 로그인되어있지않다면
			response.put("status", "login"); // 로그인 상태 알리는 응답 리턴
			return response;
		}
		
		//이미 장바구니에 담긴 상품인지 확인
		// 기존 상품인 경우, 장바구니로 이동시키기
		Optional<ShoppingCartDTO> _inCart = shoppingCartRepository.findByProductNoAndMember(cartForm.getProductNo(),dto); // 추가할 상품 번호로 장바구니 조회
		if (_inCart.isPresent()) { // 기존 장바구니 레코드가 있다면
			//이미 담긴 상품이므로 장바구니로 이동시키기
			response.put("status","inCart");
			if(productService.getProductByProductNo(cartForm.getProductNo()).getIsSubscription()==1) {
				//정기상품이라면
				response.put("url", "/order/cart/subs");
			}else {
				response.put("url", "/order/cart/normal");
			}
			return response;
		}
		
		// 1. 장바구니에 구매할 상품 추가
		shoppingCartService.insertCart(cartForm);
		// 방금 집어넣은 장바구니 상품 목록 가져오기
		ArrayList<ShoppingCartDTO> cart = shoppingCartService.getOrderNow(cartForm.getMemberNo());
		
		// 2. orderForm에 저장할 값 가져오기
		// 총 결제 금액 계산
		int price = cartForm.getPrice(); // 상품 1개당 가격
		int quantity = cartForm.getQuantity(); // 상품 개수
		int totalPrice = price * quantity; // 총 금액

		// 배송비 가져오기
		ShopDTO shop = shopRepository.findByShopNo(cartForm.getShopNo());
		// shop정보 리턴
		int deliveryFee = (int) shop.getDeliveryFee(); // shop레코드의 delivertyFee
		int orderPrice = deliveryFee + totalPrice;

		// 3. orderForm에 주문 기본값(상품 정보, 주문 정보, 배송 정보) 세팅
		OrderForm orderForm = new OrderForm(); // orderForm객체 생성
		// 상품 정보
		orderForm.setShopNo(cartForm.getShopNo()); // 상점 번호
		orderForm.setTotalPrice(totalPrice); // 상품 총액
		orderForm.setDeliveryFee(deliveryFee); // 배송비
		orderForm.setOrderPrice(orderPrice); // 현재 주문의 결제 예정 금액
		// 구매자 정보/배송지 기본값
		orderForm.setBuyerNo(dto.getMemberNo()); // 구매자번호로 memberNo
		//기본배송지의 이름/폰/주소 적용 >> 없으면 주문자 정보 사용
		AddressBookDTO address = addressBookService.getMyOnlyAddress(dto); //기본 배송지
		if(address==null) {
			orderForm.setOrderName(dto.getMemberName()); // 주문자이름으로 memberName			
			orderForm.setOrderPhone(dto.getMemberPhone()); // 주문자연락처로 memberPhone
			orderForm.setOrderAddress(dto.getMemberAddress()); // 배송지로 memberAddress
		}else { //기본배송지가 있다면
			orderForm.setOrderName(address.getReceiverName()); // 기본배송지 이름			
			orderForm.setOrderPhone(address.getReceiverPhone()); // 기본배송지 전화번호
			orderForm.setOrderAddress(address.getAddressDetail()); //기본배송지 주소
		}
		
		// 4. orderForm 세션에 저장
		session.setAttribute("orderForm", orderForm); // 주문자 정보 저장한 orderForm
		session.setAttribute("cartList", cart); // 방금 insert한 장바구니 내역
		//구매할 상품이 정기 결제 상품이라면
		if(productService.getProductByProductNo(cartForm.getProductNo()).getIsSubscription()==1) {
			session.setAttribute("isSubs", true); //isSubs추가 전달
		}
		
		// 5. 처리 성공시 응답 담기
		response.put("status", "success");
		response.put("url", "/order/order");

		return response;

	}

	// 주문 페이지 접속: localhost:8080/order/order
	// get방식으로 요청 시 메인으로 돌아가도록
	@GetMapping("order")
	public String order(Model model, HttpSession session) {

		//로그인한 사용자이고, 장바구니내역이 선택된 사용자인지 확인
		MemberDTO member = (MemberDTO) session.getAttribute("user");// 로그인한 사용자의 member꺼내기
		OrderForm orderForm = (OrderForm) session.getAttribute("orderForm");// orderForm 꺼내기
		ArrayList<ShoppingCartDTO> cartList = (ArrayList<ShoppingCartDTO>) session.getAttribute("cartList");// 장바구니 내역 꺼내기

		// 로그인하지 않았고, orderForm이나 cartList가 null 일 때
		if (member == null && (orderForm == null || cartList == null)) {
			model.addAttribute("title", "접근 제한");
			model.addAttribute("msg", "잘못된 접근입니다. 메인으로 돌아갑니다.");
			model.addAttribute("icon", "warning");
			model.addAttribute("loc", "/"); // 메인으로 이동

			return "/common/msg";
		}

		// 뷰로 데이터 전달
		model.addAttribute("orderForm", orderForm);
		model.addAttribute("list", cartList);
		if(session.getAttribute("isSubs")!=null && (boolean)session.getAttribute("isSubs")) { //구매할 상품이 정기상품이라면
			model.addAttribute("isSubs",true);
			session.removeAttribute("isSubs");
		}
		// 세션에서 데이터 삭제
		session.removeAttribute("orderForm");
		session.removeAttribute("cartList");

		return "/order/order";
	}

	// 결제 페이지 접속: localhost:8080/order/checkout
	@GetMapping("checkout")
	public String checkout(Model model) {

		// 누구든 get방식으로 요청 시 메인으로 돌아가도록
		model.addAttribute("title", "접근 제한");
		model.addAttribute("msg", "잘못된 접근입니다. 메인으로 돌아갑니다.");
		model.addAttribute("icon", "warning");
		model.addAttribute("loc", "/"); // 메인으로 이동

		return "/common/msg";
	}

	// 결제 완료 후 주문 내역 테이블에 insert처리, 장바구니 삭제
	// 매개변수: 주문정보가 담긴 orderForm
	@PostMapping("checkout")
	public String insertOrder(@RequestParam("cartNos") ArrayList<Integer> cartNos, OrderForm orderForm,
			BindingResult bindingResult, Model model) {

		//if (bindingResult.hasErrors()) { // 유효성 검사에 에러가 있다면
		//	return "/order/order";
		//}

		// 결제 검증 - 실제 가격과 결제된 가격이 일치하는지?
		// >>PortOneService.단건 조회 api호출
		// TotalPrice (form에서 넘어온 값) 과 realPrice(DB에서 꺼내온 값)
		// if) DB -> productNo 를 가지고 가격 가져와서 실제 상품의 가격과 일치하는지 검증
		// else) 가격이 일치하지 않을경우 결제 취소 , 사용자에게 잘못된 접근에 관해 경고 alert 띄워줌
		// >>결제 검증 방식 웹훅의 값을 사용하는 것으로 변경

		// 결제완료 후 주문 내역 CRUD
		String token = portOneService.getToken();
		try {
			// 토큰발급받기
			this.shoppingCartService.insertOrder(orderForm);			
		}catch(Exception e) {
			//DB작업 중 문제 발생 시
			portOneService.getRefund(token, orderForm.getMerchantUid()); //결제 취소
			//DB삭제
		}

		// 주문내역으로 이동
		model.addAttribute("title", "주문 완료");
		model.addAttribute("msg", "구매 내역 페이지로 이동합니다.");
		model.addAttribute("icon", "success");
		model.addAttribute("loc", "/member/order-list");
		return "/common/msg";
	}

	//포트원 웹훅 엔드 포인트 - 결제 후 서버로부터 응답이 도착하는 url
	//웹훅 공부 후 결제 예약 API와 함께 사용
	/*
	 * @PostMapping("checkout/webhook")
	 * 
	 * @ResponseBody public ResponseEntity<String> portOneWebhook(@RequestParam
	 * String status, @RequestParam String imp_uid, @RequestParam String
	 * merchant_uid,
	 * 
	 * @RequestParam(name="cancellation_id", required=false) String
	 * cancellation_id, @RequestBody Map<String, Object> webhook){ //RequestParam으로
	 * url의 body에 실린 값을 빼옴 >> @RequestBody로 한 방에 값 빼오기 도전 //전달받는 값 확인
	 * System.out.println("status:"+status); System.out.println("response"+webhook);
	 * 
	 * if(status.equals("paid")) { //결제가 완료됨
	 * 
	 * //customer_uid로 이전 결제 정보 찾기 //merchant_uid로 DB에서 예약 결제된 건 찾기
	 * //OrderHistoryDTO dto = //
	 * 
	 * //(사후 검증) 실결제 금액과 DB에 저장될 금액이 맞는지 확인 //merchant_uid로 DB에서 해당하는 레코드 찾기
	 * 
	 * } else if(status.equals("cancelled")) {
	 * 
	 * } return ResponseEntity.ok("응답 값 확인"); }
	 */
	
	// 주문 취소
	// 매개변수: merchant_uid, orderNo, reason 
	@PostMapping("cancel")
	@ResponseBody
	public ResponseEntity<?> ajaxCancelOrder(@RequestBody Map<String, Object> data) {
		// JSON형식을 받을 수 있는 Map타입
		try {
			// JSON형식으로 전달된 data에서 값 꺼내기
			String merchantUid = String.valueOf(data.get("merchant_uid")); // long타입을 String으로 변환
			int orderNo = (int) data.get("orderNo");
			String reason = (String) data.get("reason");

			// 토큰발급받기
			String token = portOneService.getToken();

			// 주문 상태 확인
			// 주문 객체 꺼내기
			Optional<OrderHistoryDTO> _order = orderHistoryRepository.findByOrderNo(orderNo);
			OrderHistoryDTO order = new OrderHistoryDTO(); // 주문 객체 생성
			if (_order.isPresent()) {
				order = _order.get(); // 주문 객체 대입
			}
			// 주문 status가 0이 아니라면
			if (order.getStatus() != 0) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 취소 불가");
			}

			// 결제 취소
			portOneService.getRefund(token, merchantUid);

			// DB CRUD: orderHistory상태status변경& orderTime 취소시간, 사유 입력
			shoppingCartService.cancelOrder(order, reason);

			return ResponseEntity.ok("주문 취소 성공");

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
		}
	}
	
	//정기 결제 처리 - insert 주문내역, 주문 상세 insert, 장바구니 내역 delete
	//매개변수: 결제정보가 입력된 orderForm
	@PostMapping("checkout-subs")
	public String subscriptionOrder(Model model, OrderForm orderForm, BindingResult bindingResult, HttpSession session){ 
		//@RequestParam("merchantUid") String merchantUid, @RequestParam String customerUid, @RequestParam int amount
		
		//유효성검사는 언제..
		try {
		//사용할 값 꺼내기
		String oldMerchantUid = orderForm.getMerchantUid();
		String customerUid = orderForm.getCustomerUid();
		int amount = orderForm.getOrderPrice();
		String name = orderForm.getName();
		
	//1. 주문내역, 주문 시간 내역 insert
		// 주문 내역 엔티티 생성
		OrderHistoryDTO order = new OrderHistoryDTO();
		// MemberDTO 조회
		MemberDTO member = memberRepository.findByMemberNo(orderForm.getBuyerNo());
		// ShopDTO 조회
		ShopDTO shop = shopRepository.findByShopNo(orderForm.getShopNo());
		
		//order에 값 저장
		order.setShop(shop);
		order.setMember(member);
		order.setOrderAddress(orderForm.getOrderAddress());
		order.setOrderName(orderForm.getOrderName());
		order.setOrderPhone(orderForm.getOrderPhone());
		order.setOrderRequest(orderForm.getOrderRequest());
		order.setMerchantUid(orderForm.getMerchantUid());
		order.setDeliveryFee(orderForm.getDeliveryFee());
		order.setOrderPrice(orderForm.getOrderPrice());
		//배송 주기 = 다음 배송 날짜 = 오늘 날짜 + 한달/2주/1주
		LocalDateTime DeliveryCycle=LocalDateTime.now();
		switch(orderForm.getDeliveryCycle()) {
		case 1:	//한 달
			DeliveryCycle = LocalDateTime.now().plusMonths(1); 
			break;
		case 2:	//격주
			DeliveryCycle = LocalDateTime.now().plusWeeks(2);
			break;
		case 3:	//1주
			DeliveryCycle = LocalDateTime.now().plusWeeks(1);
		}

		order.setDeliveryCycle(DeliveryCycle); 
		//빌링키
		order.setCustomerUid(orderForm.getCustomerUid());
		//다음 결제일 = 첫 결제 시간 + 한달
		order.setNextPaymentAt(LocalDateTime.now().plusMonths(1)); 		
		//정기 결제 전 알림 여부
		order.setAlert(orderForm.getAlert());
		if(order.getAlert()==null || order.getAlert().isBlank()) {
			order.setAlert("N");
		}
		order.setName(orderForm.getName());
		
		//정기결제 메서드 사용
		Map<String, Object> response = shoppingCartService.insertSubscriptionOrder(order);
		if(!(response.get("code").toString().equals("0"))) { //정기 결제 실패라면
			System.out.println(response.get("message"));
			return "/order/order";
		}
			
		//정기결제 성공 시 주문내역&주문 시간 내역 save
		//주문 시간 엔티티 생성
		OrderTimeDTO orderTime = new OrderTimeDTO();
		orderTime.setOrderedAt(LocalDateTime.now()); // 주문 insert되는 시간
		orderTime.setOrder(order); // order저장 시 동시에 insert되도록
		// orderTimeDTO를 order에 set
		order.setOrderTime(orderTime); // 주문내역DTO의 컬럼값에 cascade설정 되어있으므로 동시 저장됨
		this.orderHistoryRepository.save(order); // cascadeType.ALL설정으로 주문 내역, 주문 시간내역 동시에 저장됨
		//System.out.println("주문내역 insert"); // 1. insert확인용		
		
	//2. 주문 상세 insert
		// 1에서 insert된 주문 내역의 order가져오기
		Optional<Integer> _orderNo = orderHistoryRepository.getOrderNoByMember(member); // null일 수도 있으므로 Optional객체로 받기
		if (_orderNo.isEmpty()) { // orderNo에 값이 없다면
		}
		int orderNo = _orderNo.get();// 1에서 insert한 주문내역의 orderNo

		// OrderDetail에 set할 order객체(1에서 insert한 주문) 가져오기
		Optional<OrderHistoryDTO> _order = orderHistoryRepository.findById(orderNo);
		OrderHistoryDTO ordered = new OrderHistoryDTO(); // order담을 객체
		if (_order.isPresent()) { // _order에 값이 있다면
			ordered = _order.get(); // ordered에 대입
		}

		// 장바구니 내역에서 주문 상세에 넣을 상품 정보 조회
		List<ShoppingCartDTO> cart = shoppingCartService.getSelectedList(member.getMemberNo(), orderForm.getCartNos()); // 장바구니에서 결제된
																											// 상품 리스트
		for (ShoppingCartDTO c : cart) { // 결제된 상품 1개씩 꺼내기
			// 주문 상세 내역 엔티티 생성
			OrderDetailDTO orderDetail = new OrderDetailDTO(); // orderDetail에 상품 정보 set
			orderDetail.setOrder(ordered); // 주문번호
			orderDetail.setProductNo(c.getProductNo()); // 상품 번호
			orderDetail.setPrice(c.getPrice()); // 가격
			orderDetail.setQuantity(c.getQuantity()); // 수량
			orderDetail.setDetailReg(LocalDateTime.now()); // insert일시
			orderDetail.setDeliveryCycle(DeliveryCycle);
			orderDetailRepository.save(orderDetail); // 주문 상세 내역 insert
			//System.out.println("주문 상세내역 insert"); // 2. insert 확인용

			// 3.장바구니 내역 삭제
			shoppingCartRepository.deleteById(c.getCartNo());
			//System.out.println("장바구니에서 삭제"); // 3. delete 확인용
		} // 결제한 장바구니의 상품 1개 꺼내는 반복문 종료

		// 생성된 상세 내역& ordered의 상세 내역 list를 동시 저장)
		ordered.setOrderDetail(orderDetailRepository.findByOrderNo(orderNo));
		orderHistoryRepository.save(ordered);
		
		// 주문내역으로 이동
		model.addAttribute("title", "주문 완료");
		model.addAttribute("msg", "구매 내역 페이지로 이동합니다.");
		model.addAttribute("icon", "success");
		model.addAttribute("loc", "/member/subscription"); 
		return "/common/msg";
		
		}catch(Exception e) { //오류 발생시
			//결제 취소
			
			// 메인으로 이동
			model.addAttribute("title", "주문 실패");
			model.addAttribute("msg", "결제에 실패하여 메인으로 이동합니다.");
			model.addAttribute("icon", "error");
			model.addAttribute("loc", "/"); 
			return "/common/msg";
		}

	}

	
}
