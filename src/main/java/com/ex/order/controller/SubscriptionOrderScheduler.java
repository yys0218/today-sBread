package com.ex.order.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.data.OrderTimeDTO;
import com.ex.order.model.repository.OrderDetailRepository;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.service.ShoppingCartService;

import lombok.RequiredArgsConstructor;

@Component //해당 클래스가 스프링 Bean으로 인식되게 함
@RequiredArgsConstructor
public class SubscriptionOrderScheduler {
	
	OrderHistoryRepository orderHistoryRepository;
	ShoppingCartService shoppingCartService;
	OrderDetailRepository orderDetailRepository;
	
	//정기 결제 하루 전 알림 전송
	public void alertSubscription() {
		//웹소켓? alertHistory에 DB넣기
		
	}
	
	//정기 배송일에 새 주문내역 넣기 >> 정기 결제 메서드 사용 x / DeliveryCycle + 해당 주기 / orderPrice=0
	@Scheduled(cron = "0 0 9 * * *")//매일 오전 9시에 실행
	public void insertSubscription() {
		//오늘=배송일인 레코드 뽑기 >> List<dto>리턴
		List<OrderHistoryDTO> list = orderHistoryRepository.findByDeliveryCycle();
		//오늘=배송일인 레코드가 없다면
		if(list!=null && list.size()>0) {		
		
			//for(dto : list) { 원래 dto결제정보 저장하는 newDTO생성, 정기 결제 진행 }
			for(OrderHistoryDTO dto : list) {
				//배송일=결제일이라면, 새 주문내역 넣지 않기 >> 정기 결제 건 처리 시 새 주문이 insert됨
				if((dto.getDeliveryCycle().toLocalDate()).isEqual(dto.getNextPaymentAt().toLocalDate())) {
					break;
				}
				System.out.println(dto.getDeliveryCycle());
				
				//사용할 값 꺼내기
				String customerUid = dto.getCustomerUid();
				int amount = dto.getOrderPrice();
				String name = dto.getName();
				List<OrderDetailDTO> old = orderDetailRepository.findByOrderNo(dto.getOrderNo()); //이전 상세 내역
				
			//1. 주문내역, 주문 시간 내역 insert
				//새 주문 내역 엔티티 생성
				OrderHistoryDTO order = new OrderHistoryDTO();
				
				//order에 값 저장
				order.setShop(dto.getShop());
				order.setMember(dto.getMember());
				order.setOrderAddress(dto.getOrderAddress());
				order.setOrderName(dto.getOrderName());
				order.setOrderPhone(dto.getOrderPhone());
				order.setOrderRequest(dto.getOrderRequest());
				order.setMerchantUid(dto.getMerchantUid());
				order.setDeliveryFee(dto.getDeliveryFee());
				order.setOrderPrice(0); //월 결제 시 한 번에 결제되므로, 정산 내역에 오차를 없애기 위함.
				//배송 주기 계산
				LocalDateTime newDate = LocalDateTime.now();
				//OrderPrice = (상품가격 + 배송비) * 한 달안에 배송되는 횟수(1/2/4)
				//상품 가격
				int productPrice = 0;
				for(OrderDetailDTO od : old ) {
					//상세 상품의 가격*수량의 합
					productPrice =+ od.getPrice()*od.getQuantity();
				}
				System.out.println("상품가격"+productPrice);
				System.out.println("배송주기"+(dto.getOrderPrice()/productPrice+dto.getDeliveryFee()));
				switch(dto.getOrderPrice()/productPrice+dto.getDeliveryFee()) { 
					case 1: //한 달
						newDate = newDate.plusMonths(1);
						break;
					case 2: //2주
						newDate = newDate.plusWeeks(2);
						break;
					case 4: //1주
						newDate = newDate.plusWeeks(1);
						break;
				}
				System.out.println("다음 배송일은 "+newDate);
				//배송 주기
				order.setDeliveryCycle(newDate); 
				//빌링키
				order.setCustomerUid(dto.getCustomerUid());
				//다음 결제일 = 첫 결제 시간 + 한 달 >> 지금은 테스트용 + 하루
				order.setNextPaymentAt(LocalDateTime.now().plusDays(1)); 		
				//정기 결제 전 알림 여부
				order.setAlert(dto.getAlert());
				if(order.getAlert().isBlank() || order.getAlert()==null) {
					order.setAlert("N");
				}
				order.setName(dto.getName());
				if(order.getName().isBlank() || order.getName()==null) {
					order.setName("정기 배송");
				}	
				//정기결제 성공 시 주문내역&주문 시간 내역 save
				//주문 시간 엔티티 생성
				OrderTimeDTO orderTime = new OrderTimeDTO();
				orderTime.setOrderedAt(LocalDateTime.now()); // 주문 insert되는 시간
				orderTime.setOrder(order); // order저장 시 동시에 insert되도록
				// orderTimeDTO를 order에 set
				order.setOrderTime(orderTime); // 주문내역DTO의 컬럼값에 cascade설정 되어있으므로 동시 저장됨
				this.orderHistoryRepository.save(order); // cascadeType.ALL설정으로 주문 내역, 주문 시간내역 동시에 저장됨
				System.out.println("주문내역 insert"); // 1. insert확인용		
				
			//2. 주문 상세 insert
				// 1에서 insert된 주문 내역의 order가져오기
				Optional<Integer> _orderNo = orderHistoryRepository.getOrderNoByMember(dto.getMember()); // null일 수도 있으므로 Optional객체로 받기
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
	
				// 이전 상세 내역 주문번호로 새 상세내역에 넣을 상품 정보 조회
				for (OrderDetailDTO o : old) { // 결제된 상품 1개씩 꺼내기
					// 주문 상세 내역 엔티티 생성
					OrderDetailDTO orderDetail = new OrderDetailDTO(); // orderDetail에 상품 정보 set
					orderDetail.setOrder(ordered); // 주문번호
					orderDetail.setProductNo(o.getProductNo()); // 상품 번호
					orderDetail.setPrice(o.getPrice()); // 가격
					orderDetail.setQuantity(o.getQuantity()); // 수량
					orderDetail.setDetailReg(LocalDateTime.now()); // insert일시
					orderDetail.setDeliveryCycle(newDate);
					orderDetailRepository.save(orderDetail); // 주문 상세 내역 insert
					System.out.println("주문 상세내역 insert"); // 2. insert 확인용
				} // 결제한 장바구니의 상품 1개 꺼내는 반복문 종료
	
				// 생성된 상세 내역 담을 리스트 생성 (ordered에 저장용)
				ordered.setOrderDetail(orderDetailRepository.findByOrderNo(orderNo));
				orderHistoryRepository.save(ordered);	
			}		
		}
	}
	
	//정기 결제일에 새 주문내역 넣기 >>nextPaymentAt +한 달 / 배송주기 업데이트 / OrderPrice에 결제 금액 넣기
	@Scheduled(cron = "0 0 11 * * *")//매일 오전 11시에 실행
	public void checkoutSubscription() {
		//오늘=결제일 인 레코드 뽑기 >> List<dto>리턴
		List<OrderHistoryDTO> list = orderHistoryRepository.findByNextPaymentAt();
		//오늘=결제일인 레코드가 있다면
		if(list!=null && list.size()>0) {
			
			//for(dto : list) { 원래 dto결제정보 저장하는 newDTO생성, 정기 결제 진행 }
			for(OrderHistoryDTO dto : list) {
				//사용할 값 꺼내기
				String customerUid = dto.getCustomerUid();
				String name = dto.getName();
				//상품 총 결제 예정 금액 계산 위함
				List<OrderDetailDTO> old = orderDetailRepository.findByOrderNo(dto.getOrderNo());
				int amount = 0; //결제 금액
				for(OrderDetailDTO od : old ) {
					//상세 상품의 가격*수량의 합
					amount =+ od.getPrice()*od.getQuantity();
				}
				amount=amount+dto.getDeliveryFee();
				System.out.println("결제 예정 금액="+amount);
				
			//1. 주문내역, 주문 시간 내역 insert
				//새 주문 내역 엔티티 생성
				OrderHistoryDTO order = new OrderHistoryDTO();
				
				//order에 값 저장
				order.setShop(dto.getShop());
				order.setMember(dto.getMember());
				order.setOrderAddress(dto.getOrderAddress());
				order.setOrderName(dto.getOrderName());
				order.setOrderPhone(dto.getOrderPhone());
				order.setOrderRequest(dto.getOrderRequest());
				order.setMerchantUid(dto.getMerchantUid());
				order.setDeliveryFee(dto.getDeliveryFee());
				order.setOrderPrice(amount);
				//배송 주기 계산
				LocalDateTime newDate = LocalDateTime.now();
				//OrderPrice = (상품가격 + 배송비) * 한 달안에 배송되는 횟수(1/2/4)
				//상품 가격
				int productPrice = 0;
				for(OrderDetailDTO od : old ) {
					//상세 상품의 가격*수량의 합
					productPrice =+ od.getPrice()*od.getQuantity();
				}
				System.out.println("상품가격"+productPrice);
				System.out.println("배송주기"+(amount/productPrice+dto.getDeliveryFee()));
				switch(amount/productPrice+dto.getDeliveryFee()) { 
					case 1: //한 달
						newDate = newDate.plusMonths(1);
						break;
					case 2: //2주
						newDate = newDate.plusWeeks(2);
						break;
					case 4: //1주
						newDate = newDate.plusWeeks(1);
						break;
				}
				System.out.println("다음 배송일은 "+newDate);
				order.setDeliveryCycle(newDate); 
				//빌링키
				order.setCustomerUid(dto.getCustomerUid());
				//다음 결제일 = 첫 결제 시간 + 한 달 >> 지금은 테스트용 + 하루
				order.setNextPaymentAt(LocalDateTime.now().plusDays(1)); 		
				//정기 결제 전 알림 여부
				order.setAlert(dto.getAlert());
				if(order.getAlert().isBlank() || order.getAlert()==null) {
					order.setAlert("N");
				}
				order.setName(dto.getName());
				
				//정기결제 메서드 사용
				Map<String, Object> response = shoppingCartService.insertSubscriptionOrder(order);
				if(!(response.get("code").toString().equals("0"))) { //정기 결제 실패라면
					System.out.println(response.get("message"));
				}
					
				//정기결제 성공 시 주문내역&주문 시간 내역 save
				//주문 시간 엔티티 생성
				OrderTimeDTO orderTime = new OrderTimeDTO();
				orderTime.setOrderedAt(LocalDateTime.now()); // 주문 insert되는 시간
				orderTime.setOrder(order); // order저장 시 동시에 insert되도록
				// orderTimeDTO를 order에 set
				order.setOrderTime(orderTime); // 주문내역DTO의 컬럼값에 cascade설정 되어있으므로 동시 저장됨
				this.orderHistoryRepository.save(order); // cascadeType.ALL설정으로 주문 내역, 주문 시간내역 동시에 저장됨
				System.out.println("주문내역 insert"); // 1. insert확인용		
				
			//2. 주문 상세 insert
				// 1에서 insert된 주문 내역의 order가져오기
				Optional<Integer> _orderNo = orderHistoryRepository.getOrderNoByMember(dto.getMember()); // null일 수도 있으므로 Optional객체로 받기
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
	
				// 이전 상세 내역 주문번호로 새 상세내역에 넣을 상품 정보 조회
				for (OrderDetailDTO o : old) { // 결제된 상품 1개씩 꺼내기
					// 주문 상세 내역 엔티티 생성
					OrderDetailDTO orderDetail = new OrderDetailDTO(); // orderDetail에 상품 정보 set
					orderDetail.setOrder(ordered); // 주문번호
					orderDetail.setProductNo(o.getProductNo()); // 상품 번호
					orderDetail.setPrice(o.getPrice()); // 가격
					orderDetail.setQuantity(o.getQuantity()); // 수량
					orderDetail.setDetailReg(LocalDateTime.now()); // insert일시
					orderDetail.setDeliveryCycle(newDate);
					orderDetailRepository.save(orderDetail); // 주문 상세 내역 insert
					System.out.println("주문 상세내역 insert"); // 2. insert 확인용
				} // 결제한 장바구니의 상품 1개 꺼내는 반복문 종료
	
				// 생성된 상세 내역 담을 리스트 생성 (ordered에 저장용)
				ordered.setOrderDetail(orderDetailRepository.findByOrderNo(orderNo));
				orderHistoryRepository.save(ordered);	
			}
		}
	
	}
	
	
}
