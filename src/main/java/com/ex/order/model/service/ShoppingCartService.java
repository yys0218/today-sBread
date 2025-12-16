package com.ex.order.model.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderForm;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.data.OrderTimeDTO;
import com.ex.order.model.data.ScheduleAnnotation;
import com.ex.order.model.data.ShoppingCartDTO;
import com.ex.order.model.data.ShoppingCartForm;
import com.ex.order.model.repository.OrderDetailRepository;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.repository.OrderTimeRepository;
import com.ex.order.model.repository.ShoppingCartRepository;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.repository.ProductMapper;
import com.ex.product.model.service.ProductService;
import com.ex.rider.model.data.DeliveryFeeDTO;
import com.ex.rider.model.repository.DeliveryFeeRepository;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

	private final ShoppingCartRepository cartRepository;
	private final MemberRepository memberRepository;
	private final ShopRepository shopRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final OrderDetailRepository orderDetailRepository;
	private final OrderTimeRepository orderTimeRepository;
	private final ProductService productService;
	private final ProductMapper productMapper;
	private final OrderHistoryRepository orderRepo;
	private final DeliveryFeeRepository deliveryFeeRepository;
	private final PortOneService portOneService;
	private final UserDetailsService userDetailsService;

	// 장바구니에 상품 추가
	// 필요 매개변수: shoppingCartForm
	public void insertCart(ShoppingCartForm form) {
		// shop객체 가져오기
		Optional<ShopDTO> _shop = shopRepository.findById(form.getShopNo());// shop객체 조회
		ShopDTO shop = new ShopDTO(); // shop담을 객체 생성
		if (_shop.isPresent()) { // _shop에 값이 있다면
			shop = _shop.get(); // shop에 대입
		}

		// member객체 가져오기(구매자 정보)
		Optional<MemberDTO> _member = memberRepository.findById(form.getMemberNo());// member객체 조회
		MemberDTO member = new MemberDTO();
		if (_member.isPresent()) { // _member에 값이 있다면
			member = _member.get(); // member에 대입
		}

		// thumbnailName가져오기
		ProductDTO product = productService.product(form.getProductNo()); // product객체 조회
		// 장바구니 내역에 insert
		ShoppingCartDTO dto = new ShoppingCartDTO(); // shoppingCartDTO생성
		dto.setProductNo(form.getProductNo()); // form객체에서 값 꺼내 전달
		dto.setProductName(form.getProductName());
		dto.setQuantity(form.getQuantity());
		dto.setPrice(form.getPrice());
		dto.setThumbnailName(product.getThumbnailName());
		dto.setShop(shop);
		dto.setMember(member);
		dto.setCartReg(LocalDateTime.now());
		this.cartRepository.save(dto); // dto를 DB에 insert

	}

	// 장바구니 목록 가져오기
	// 매개변수: memberNo
	// public ArrayList<ShoppingCartDTO> getCartList(int memberNo) {
	// member조회
	// Optional<MemberDTO> _member = memberRepository.findById(memberNo);
	// MemberDTO member = new MemberDTO();
	// if (_member.isPresent()) {
	// member = _member.get();
	// }
	// return this.cartRepository.findByMember(member);
	// }

	// 일반/정기 상품 나누어 장바구니 목록 가져오기
	// 매개변수: ShopDTO, MemberDTO, int isSubs(정기상품인지 아닌지)
	public ArrayList<ShoppingCartDTO> getCartList(ShopDTO shop, MemberDTO member, int isSubs) {
		// 결과로 리턴할 장바구니 목록 변수
		ArrayList<ShoppingCartDTO> result = new ArrayList<ShoppingCartDTO>();

		// isSubs에 따라 분류할 장바구니 목록 가져오기
		ArrayList<ShoppingCartDTO> list = cartRepository.findByShopAndMember(shop, member);
		for (ShoppingCartDTO dto : list) { // 내역 하나씩 꺼내기
			ProductDTO product = productService.getProductByProductNo(dto.getProductNo()); // 내역의 상품꺼내기
			if (product == null) {
            // 상품이 삭제되었을 경우: 스킵
            continue;
        }
			dto.setStatus(product.getStatus()); // 상품의 판매상태 장바구니 내역에 저장
			if (product.getIsSubscription() == isSubs) { // isSubs에 해당하는 상품이라면
				result.add(dto); // 결과 목록에 추가
			}
		}
		return result;
	}

	// 결제할 상품 정보 update
	// 매개변수: cartNo, quantity, memberNo
	public void updateCart(int cartNo, int quantity, int memberNo) {
		// member 조회
		Optional<MemberDTO> _member = memberRepository.findById(memberNo);
		MemberDTO member = new MemberDTO();
		if (_member.isPresent()) {
			member = _member.get();
		}
		System.out.println("update"+quantity);
		// memberNo, cartNo로 조회 (레코드 리턴)
		ShoppingCartDTO dto = this.cartRepository.findByMemberAndCartNo(member, cartNo);
		// update CRUD
		dto.setQuantity(quantity); // quantity set
		// dto.setOption(option); //옵션 set
		cartRepository.save(dto);// DB에 save
	}

	// 선택한 장바구니 내역 조회
	// 매개변수: cartNos, memberNo
	public ArrayList<ShoppingCartDTO> getSelectedList(int memberNo, ArrayList<Integer> cartNos) {

		// member 조회
		Optional<MemberDTO> _member = memberRepository.findById(memberNo);
		MemberDTO member = new MemberDTO();
		if (_member.isPresent()) {
			member = _member.get();
		}

		// 선택된 장바구니 조회
		ArrayList<ShoppingCartDTO> selectedList = new ArrayList<ShoppingCartDTO>(); // 결과값 담을 객체 생성
		for (int cartNo : cartNos) {// cartNos에서 cartNo추출
			selectedList.add(this.cartRepository.findByMemberAndCartNo(member, cartNo)); // 조건에 해당하는 dto 결과값에 추가
		}
		return selectedList;
	}

	// 주문할 장바구니 내역 바로 주문하기
	// 매개변수: memberNo
	public ArrayList<ShoppingCartDTO> getOrderNow(int memberNo) {

		// member 조회
		Optional<MemberDTO> _member = memberRepository.findById(memberNo);
		MemberDTO member = new MemberDTO();
		if (_member.isPresent()) {
			member = _member.get();
		}

		// 장바구니 조회
		ArrayList<ShoppingCartDTO> orderNow = new ArrayList<ShoppingCartDTO>(); // 결과값 담을 객체 생성
		// 가장 최근 장바구니 번호 조회
		Optional<Integer> _cartNo = this.cartRepository.getCartNoByMember(member);
		int cartNo = 0;
		if (_cartNo.isPresent()) {
			cartNo = _cartNo.get();
		}
		orderNow.add(cartRepository.findByMemberAndCartNo(member, cartNo)); // 조건에 해당하는 dto 결과값에 추가
		return orderNow;

	}

	// 결제완료 후 주문 내역 기록, 장바구니 삭제
	// 매개변수: orderForm
	public void insertOrder(OrderForm orderForm) {
		//문제발생 시 결제 취소용
		String token = portOneService.getToken();
		
		// 1. 주문 내역, 주문 시간 내역 insert 2.주문 상세 내역 insert 3.장바구니 내역 delete
		try {
			// 1. 주문내역, 주문 시간 내역 insert
			// 주문 내역 엔티티 생성
			OrderHistoryDTO order = new OrderHistoryDTO();
			// MemberDTO 조회
			MemberDTO member = memberRepository.findByMemberNo(orderForm.getBuyerNo());
			// ShopDTO 조회
			ShopDTO shop = shopRepository.findByShopNo(orderForm.getShopNo());
	
			// orderForm에서 값 꺼내 order에 set
			order.setMember(member); // 구매자 정보
			order.setDeliveryFee(orderForm.getDeliveryFee());
			order.setMerchantUid(orderForm.getMerchantUid());
			order.setOrderAddress(orderForm.getOrderAddress());
			order.setOrderPrice(orderForm.getOrderPrice());
			order.setOrderPhone(orderForm.getOrderPhone());
			order.setOrderRequest(orderForm.getOrderRequest());
			order.setOrderName(orderForm.getOrderName());
			order.setShop(shop); // 판매자(가게) 정보
	
			// 주문 시간 엔티티 생성
			OrderTimeDTO orderTime = new OrderTimeDTO();
			orderTime.setOrderedAt(LocalDateTime.now()); // 주문 insert되는 시간
			orderTime.setOrder(order); // order저장 시 동시에 insert되도록
			// orderTimeDTO를 order에 set
			order.setOrderTime(orderTime); // 주문내역DTO의 컬럼값에 cascade설정 되어있으므로 동시 저장됨
			this.orderHistoryRepository.save(order); // cascadeType.ALL설정으로 주문 내역, 주문 시간내역 동시에 저장됨
	
			// 2.주문 상세 내역 insert
			// 1에서 insert된 주문 내역의 order가져오기
			Optional<Integer> _orderNo = orderHistoryRepository.getOrderNoByMember(member); // null일 수도 있으므로 Optional객체로 받기
			if (_orderNo.isEmpty()) { // orderNo에 값이 없다면
				System.out.println("주문내역 조회 불가"); // 오류 확인용
			}
			int orderNo = _orderNo.get();// 1에서 insert한 주문내역의 orderNo
	
			// OrderDetail에 set할 order객체(1에서 insert한 주문) 가져오기
			Optional<OrderHistoryDTO> _order = orderHistoryRepository.findById(orderNo);
			OrderHistoryDTO ordered = new OrderHistoryDTO(); // order담을 객체
			if (_order.isPresent()) { // _order에 값이 있다면
				ordered = _order.get(); // ordered에 대입
			}
	
			// 장바구니 내역에서 주문 상세에 넣을 상품 정보 조회
			List<ShoppingCartDTO> cart = this.getSelectedList(member.getMemberNo(), orderForm.getCartNos()); // 장바구니에서 결제된
																												// 상품 리스트
			for (ShoppingCartDTO c : cart) { // 결제된 상품 1개씩 꺼내기
				// 주문 상세 내역 엔티티 생성
				OrderDetailDTO orderDetail = new OrderDetailDTO(); // orderDetail에 상품 정보 set
				orderDetail.setOrder(ordered); // 주문번호
				orderDetail.setProductNo(c.getProductNo()); // 상품 번호
				orderDetail.setPrice(c.getPrice()); // 가격
				orderDetail.setQuantity(c.getQuantity()); // 수량
				orderDetail.setDetailReg(LocalDateTime.now()); // insert일시
				orderDetailRepository.save(orderDetail); // 주문 상세 내역 insert
				System.out.println("주문 상세내역 insert"); // 2. insert 확인용
	
				// 3.장바구니 내역 삭제
				cartRepository.deleteById(c.getCartNo());
				System.out.println("장바구니에서 삭제"); // 3. delete 확인용
			} // 결제한 장바구니의 상품 1개 꺼내는 반복문 종료
	
			// 생성된 상세 내역 담을 리스트 생성 (ordered에 저장용)
			ordered.setOrderDetail(orderDetailRepository.findByOrderNo(orderNo));
			orderHistoryRepository.save(ordered);
		}catch(Exception e) {
			//DB작업 중 문제 발생 시
			portOneService.getRefund(token, orderForm.getMerchantUid()); //결제 취소
		}
	}

	public void dummyOrder() {
		Random random = new Random();

		// ✅ 더미 주문 100건 생성
		for (int i = 1; i <= 100; i++) {
			// 1. 주문내역 생성
			OrderHistoryDTO order = new OrderHistoryDTO();

			// 구매자 번호 (41 ~ 60번 회원 중 랜덤)
			int memberNo = 41 + random.nextInt(20); // 41~60
			MemberDTO member = memberRepository.findByMemberNo(memberNo);

			// 상점 번호 (4 ~ 20 중 랜덤)
			int shopNo = 4 + random.nextInt(17); // 4~20
			ShopDTO shop = shopRepository.findByShopNo(shopNo);

			order.setMember(member);
			order.setShop(shop);
			order.setDeliveryFee(3000); // 배달비 (고정 예시)
			order.setMerchantUid(String.valueOf(System.currentTimeMillis()) + "_" + i);
			order.setOrderAddress(member.getMemberAddress());
			order.setOrderPrice(0); // 상세내역 합산 후 업데이트
			order.setOrderPhone(member.getMemberPhone());
			order.setOrderRequest("빠른 배송 부탁드립니다");
			order.setOrderName(member.getMemberName());

			// 주문 시간 엔티티
			OrderTimeDTO orderTime = new OrderTimeDTO();
			orderTime.setOrderedAt(LocalDateTime.now().minusMinutes(random.nextInt(5000)));
			order.setOrderTime(orderTime);

			// 주문내역 저장
			orderHistoryRepository.save(order);

			// 2. 해당 상점의 상품 리스트 조회
			List<ProductDTO> productList = productMapper.findByShopNo(shopNo);
			if (productList == null || productList.isEmpty()) {
				System.out.println("상점 " + shopNo + " 에 상품 없음, 주문 스킵");
				continue;
			}

			// 3. 주문 상세 생성
			int totalPrice = 0;
			int productCount = 1 + random.nextInt(3); // 주문 상품 개수 1~3개

			Set<Integer> usedProducts = new HashSet<>(); // ✅ 이미 선택된 상품 저장

			for (int j = 0; j < productCount; j++) {
				ProductDTO product;
				do {
					product = productList.get(random.nextInt(productList.size()));
				} while (usedProducts.contains(product.getProductNo())); // ✅ 중복 상품 방지

				usedProducts.add(product.getProductNo());

				int quantity = 1 + random.nextInt(3); // 수량 랜덤

				OrderDetailDTO orderDetail = new OrderDetailDTO();
				orderDetail.setOrder(order);
				orderDetail.setProductNo(product.getProductNo());
				orderDetail.setPrice(product.getPrice());
				orderDetail.setQuantity(quantity);
				orderDetail.setDetailReg(LocalDateTime.now());
				orderDetailRepository.save(orderDetail);

				totalPrice += product.getPrice() * quantity;
			}

			// 총 결제 금액 업데이트
			order.setOrderPrice(totalPrice + order.getDeliveryFee());
			orderHistoryRepository.save(order);

			System.out.println("더미 주문 생성 완료 → 주문번호: " + order.getOrderNo() + ", 상점: " + shopNo + ", 회원: " + memberNo);
		}
	}

	// 라이더 무한 스크롤 오더 가져오기
	public List<OrderHistoryDTO> getMoreScroll(int orderNo, String orderType, int status) {
		List<OrderHistoryDTO> result;
		if (orderType.equals("all")) {

			result = orderHistoryRepository
					.findTop10ByStatusGreaterThanEqualAndOrderNoLessThanOrderByOrderNoDesc(status, orderNo);
		} else {
			result = orderHistoryRepository.findTop10ByStatusAndOrderNoLessThanOrderByOrderNoDesc(
					status,
					orderNo);
		}

		if (result.isEmpty()) {
			// 데이터 없을 경우 → 빈 리스트 반환 (프론트에서 "더 이상 없음" 처리)
			return Collections.emptyList();
		}
		return result;
	}

	// 주문 취소 시 주문내역 주문 시간 update
	// 매개변수: order객체, 주문 취소 사유
	public void cancelOrder(OrderHistoryDTO order, String reason) {
		// 주문내역 status 변경
		order.setStatus(-1);// -1(취소)로 변경
		// 주문 시간 레코드 가져오기
		Optional<OrderTimeDTO> _orderTime = orderTimeRepository.findByOrder(order);
		OrderTimeDTO orderTime = new OrderTimeDTO();
		if (_orderTime.isPresent()) {
			orderTime = _orderTime.get(); // orderTime에 값 대입
		}
		orderTime.setCanceledAt(LocalDateTime.now()); // 취소 시각에 현재 시각
		orderTime.setCancelReason(reason); // 취소 사유 저장

		orderHistoryRepository.save(order); // 동시에 update
	}

	// 정기결제 처리 메서드
	// 매개변수: 정기 결제 정보가 담긴 orderHistoryDTO (메서드 성공시 저장될 새 주문내역)
	public Map<String, Object> insertSubscriptionOrder(OrderHistoryDTO order) {
		// api사용 위한 액세스 토큰 발급
		String token = portOneService.getToken();

		// 빌링키 결제 처리 (정기결제)
		// 매개변수: 토큰, merchantUid(매번 달라야함), customerUid(매번 같아야함), 결제 금액, 주문명
		// 빌링키 결제용 새 merchantUid = 기존값/2
		String merchantUid = String.valueOf(Long.parseLong(order.getMerchantUid()) / 2);
		// 빌링키
		String customerUid = order.getCustomerUid();
		// 결제 금액
		int amount = order.getOrderPrice();
		// 주문명
		String name = "정기 배송 상품 결제";

		// 빌링키 결제 API에 실결제 요청
		Map<String, Object> response = portOneService.subscriptionOrder(token, merchantUid, customerUid, amount, name);
		String code = response.get("code").toString();

		if (code.equals("0")) { // 응답 코드 0일 경우 = 결제 성공
			// 지정된 예약일로 다음 결제를 예약
			// 매개변수: 액세스 토큰, 주문자의 customerUid, 결제정보[] = { 새로운 merchantUid, nextPaymentAt(다음
			// 결제 예정일), 금액 }
			// 결제 정보
			// 다음 결제에 사용될 merchantUid = 기존값*3
			// String newMerchantUid = String.valueOf(Long.parseLong(merchantUid)*3);
			// 다음 결제일 = 결제예정일을 초단위 timestamp형식의 long타입 정수로 변환
			// long scheduledAt =
			// (order.getNextPaymentAt()).atZone(ZoneId.systemDefault()).toEpochSecond();
			// 결제정보[]생성, 저장 - merchant_uid, 결제예정시각, 결제 예정 금액을 필수고정값으로 생성
			// ScheduleAnnotation sa = new ScheduleAnnotation(newMerchantUid, scheduledAt,
			// amount);
			// sa.setName(orderForm.);//주문명 저장
			// 예약 정보를 요소로 하는 배열 생성
			// ScheduleAnnotation[] schedules = {sa};

			// 결제 예약 API 사용
			// Map<String, Object> res = portOneService.scheduleOrder(token, customerUid,
			// schedules);
			// String resCode = res.get("code").toString();
			// if(!resCode.equals("0")) { //결제 예약 실패 시
			// System.out.println("결제 예약 실패");
			// System.out.println("코드"+resCode);
			// System.out.println(res.get("message").toString());
			// return res;
			// }
			// System.out.println("결제 예약 성공");//디버깅
		} else { // 결제 실패 시
			System.out.println("결제 실패");
			System.out.println("코드" + code);
			System.out.println(response.get("message").toString());
			return response;
		}
		System.out.println("결제 성공"); // 디버깅
		// 빌링키 결제 성공시 code=0
		return response;
	}

	public OrderHistoryDTO ajaxOrderDetail(int orderNo) {
		Optional<OrderHistoryDTO> _dto = orderHistoryRepository.findByOrderNo(orderNo);
		if (_dto.isEmpty()) {
			return null;
		} else {
			List<OrderDetailDTO> detail = orderDetailRepository.findByOrderNo(_dto.get().getOrderNo());
			for (int i = 0; i < detail.size(); i++) {
				ProductDTO product = productService.getProductByProductNo(detail.get(i).getProductNo());
				detail.get(i).setProduct(product);
			}
			_dto.get().setOrderDetail(detail);
			return _dto.get();
		}
	}

	public OrderHistoryDTO ajaxOrderAccept(int orderNo) {
		Optional<OrderHistoryDTO> _dto = orderHistoryRepository.findByOrderNo(orderNo);

		if (_dto.isEmpty()) {
			return null;
		} else {
			return _dto.get();
		}
	}

	// 주문 조회
	public OrderHistoryDTO getOrderBySalesNo(int salesNo) {
		return orderRepo.findById(salesNo).orElse(null);
	}

	// 주문 상태 업데이트
	public void updateOrderStatus(OrderHistoryDTO order) {
		orderRepo.save(order);
	}

	/**
	 * 상품 픽업 완료 버튼 클릭 시
	 * 
	 * @param orderNo (상품 번호)
	 * @return 다음 문자열 중 하나를 반환합니다:
	 *         <ul>
	 *         <li><b>"success"</b> : 정상적으로 상품을 픽업했을 경우</li>
	 *         <li><b>"fail"</b> : 상품 픽업에 실패했을 경우</li>
	 *         <li><b>"orderNotFound"</b> : 상품을 찾을 수 없을 때</li>
	 *         <li><b>"statusNot3"</b> : 라이더가 수락한 요청이 아닐 때</li>
	 *         </ul>
	 */
	public String ajaxPickupAccept(int orderNo, int durationMin) {
		Optional<OrderHistoryDTO> _dto = orderHistoryRepository.findByOrderNo(orderNo);

		if (_dto.isEmpty()) {
			return "orderNotFound"; // 상품이 비어있는경우 (상품을 찾을 수 없을 때)
		} else {
			OrderHistoryDTO dto = _dto.get();
			int status = dto.getStatus();
			if (status == 3) {
				dto.setStatus(status + 1);
				try {
					dto.getOrderTime().setPickupAt(LocalDateTime.now());
					dto.getOrderTime().setEstDeliveryAt(LocalDateTime.now().plusMinutes(durationMin));
					this.orderHistoryRepository.save(dto);

					return "success";
				} catch (Exception e) {
					return "fail";
				}
			} else {
				return "statusNot3";
			} // - if문 종료
		} // - else문 종료
	}// - 메서드 종료

	/**
	 * 배송 완료 사진 업로드 및 배송 완료 처리
	 * 
	 * @param orderNo  (상품 번호)
	 * @param filePath (사진 경로+사진명)
	 * @return 다음 문자열 중 하나를 반환합니다:
	 *         <ul>
	 *         <li><b>"success"</b> : 정상적으로 상품을 픽업했을 경우</li>
	 *         <li><b>"fail"</b> : 상품 픽업에 실패했을 경우</li>
	 *         <li><b>"orderNotFound"</b> : 상품을 찾을 수 없을 때</li>
	 *         <li><b>"statusNot3"</b> : 라이더가 수락한 요청이 아닐 때</li>
	 *         </ul>
	 */

	public String ajaxCompleteAccept(int orderNo, String filePath) {
		Optional<OrderHistoryDTO> _dto = this.orderHistoryRepository.findByOrderNo(orderNo);
		if (_dto.isEmpty()) {
			return "orderNotFound";
		} else {
			OrderHistoryDTO dto = _dto.get();
			dto.setFilepath(filePath);
			dto.setStatus(5);
			dto.getOrderTime().setCompletedAt(LocalDateTime.now());
			MemberDTO member = dto.getRider();

			orderHistoryRepository.save(dto);

			Optional<DeliveryFeeDTO> _dfDto = this.deliveryFeeRepository
					.findTop1ByMemberOrderByFeeNoDesc(member);
			if (_dfDto.isEmpty()) {
				DeliveryFeeDTO dfDto = new DeliveryFeeDTO();
				dfDto.setFeeType(1);
				dfDto.setOrderHistory(dto);
				dfDto.setMember(dto.getRider());
				dfDto.setFeeAmount(dto.getDeliveryFee());
				dfDto.setFeeBalance(dto.getDeliveryFee());
				dfDto.setCreatedAt(LocalDateTime.now());
				this.deliveryFeeRepository.save(dfDto);
			} else {
				DeliveryFeeDTO beDfDto = _dfDto.get();
				DeliveryFeeDTO newDfDto = new DeliveryFeeDTO();
				newDfDto.setFeeType(1);
				newDfDto.setOrderHistory(dto);
				newDfDto.setMember(dto.getRider());
				newDfDto.setFeeAmount(dto.getDeliveryFee());
				newDfDto.setFeeBalance(beDfDto.getFeeBalance() + dto.getDeliveryFee());
				newDfDto.setCreatedAt(LocalDateTime.now());
				this.deliveryFeeRepository.save(newDfDto);
			}
			UserDetails updatedUser = userDetailsService.loadUserByUsername(member.getMemberId());
			Authentication newAuth = new UsernamePasswordAuthenticationToken(
					updatedUser,
					updatedUser.getPassword(),
					updatedUser.getAuthorities());

			SecurityContextHolder.getContext().setAuthentication(newAuth);
			return "success";
		}
	}

	// 작업자 : 안성진
	// 정기 결제 구독 해지
	// 빌링키 삭제 또는 구독 취소 호출
	@Transactional
	public void cancelSubscription(OrderHistoryDTO order) {
		String token = portOneService.getToken();

		try {
			portOneService.deleteBillingKey(token, order.getCustomerUid());
		} catch (Exception e) {
			throw new IllegalStateException("결제사 빌링키 해지에 실패했습니다.");
		}

		// 로컬 상태 비활성화
		order.setCustomerUid(null);
		order.setNextPaymentAt(null);
		orderHistoryRepository.save(order);
	}
}
