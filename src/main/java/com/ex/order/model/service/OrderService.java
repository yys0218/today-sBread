package com.ex.order.model.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.repository.OrderDetailRepository;
import com.ex.order.model.repository.OrderRepository;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.repository.ProductMapper;
import com.ex.shop.model.data.ShopDTO;

@Service
public class OrderService { 

    private final OrderRepository orderRepository;
    private final ProductMapper productMapper; 
    private final OrderDetailRepository orderDetailRepository;

    public OrderService(OrderRepository orderRepository, ProductMapper productMapper, OrderDetailRepository orderDetailRepository) {
        this.orderRepository = orderRepository;
        this.productMapper = productMapper;
		this.orderDetailRepository = orderDetailRepository;
    }

    // 단일 주문 조회
    public OrderHistoryDTO findById(int orderNo) {
        return orderRepository.findById(orderNo).orElse(null);
    }

    // 주문 저장
    public OrderHistoryDTO save(OrderHistoryDTO order) {
        return orderRepository.save(order);
    }

    // 주문 수락 (STATUS=2)	[ 작업자 : 맹재희 ]
    @Transactional
    public void acceptOrder(int orderNo) {
        OrderHistoryDTO order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setStatus(2); // 배송 진행중
        if (order.getOrderTime() != null) {
            order.getOrderTime().setRequestedAt(LocalDateTime.now());
        }
        orderRepository.save(order);
    }

    // 주문 거절 (STATUS=4)	[ 작업자 : 맹재희 ]
    @Transactional
    public void rejectOrder(int orderNo) {
        OrderHistoryDTO order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setStatus(4); // 주문 거절
        if (order.getOrderTime() != null) {
            order.getOrderTime().setRejectedAt(LocalDateTime.now());
        }
        orderRepository.save(order);
    }

    // 판매 내역 조회 (STATUS 0~2)	[ 작업자 : 맹재희 ] >> 윤예솔
    public List<OrderHistoryDTO> getSalesOrders(int shopNo) {
        //상태가 0~2(새 주문~주문수락인)shop의 판매 내역
    	List<OrderHistoryDTO> orders = orderRepository.findByShopShopNoAndStatusBetweenOrderByOrderTimeOrderTimeNoDesc(shopNo,0,2);

        for (OrderHistoryDTO order : orders) { //한 주문당 반복
        	int orderNo = order.getOrderNo();
        	//주문번호에 해당하는 판매 상세 내역 조회
        	List<OrderDetailDTO> d = orderDetailRepository.findByOrderNo(orderNo);
                for(OrderDetailDTO dd : d) { //상세내역 1개의
                	//상품 꺼내기
                	ProductDTO p = productMapper.getProduct(dd.getProductNo());
                	//상세 레코드에 상품 저장
                	dd.setProduct(p);
                }
                //판매 내역에 상세 리스트 다시 저장
                order.setOrderDetail(d);
            }
        return orders;
        }
    
    //판매 내역 조회 (status-1~5) 작업자: 윤예솔
    public List<OrderHistoryDTO> getAllOrders(int shopNo) {
        //상태가 0~2(새 주문~주문수락인)shop의 판매 내역
    	List<OrderHistoryDTO> orders = orderRepository.findByShopShopNoAndStatusBetweenOrderByOrderTimeOrderTimeNoDesc(shopNo,-1,5);
    
	    for (OrderHistoryDTO order : orders) { //한 주문당 반복
	    	int orderNo = order.getOrderNo();
	    	//주문번호에 해당하는 판매 상세 내역 조회
	    	List<OrderDetailDTO> d = orderDetailRepository.findByOrderNo(orderNo);
	            for(OrderDetailDTO dd : d) { //상세내역 1개의
	            	//상품 꺼내기
	            	ProductDTO p = productMapper.getProduct(dd.getProductNo());
	            	//상세 레코드에 상품 저장
	            	dd.setProduct(p);
	            }
	            //판매 내역에 상세 리스트 다시 저장
	            order.setOrderDetail(d);
	        }
	    return orders;
    }
    

    // 모든 주문 대표 상품 내역 조회	[ 작업자 : 맹재희 ]
    public List<OrderHistoryDTO> getAllOrdersByShop(int shopNo) {
        List<OrderHistoryDTO> orders = orderRepository.findByShopShopNoAndStatusBetweenOrderByOrderTimeOrderTimeNoDesc(shopNo,-1,5);

        for (OrderHistoryDTO order : orders) {
            if (order.getOrderDetail() != null && !order.getOrderDetail().isEmpty()) {
                OrderDetailDTO firstDetail = order.getOrderDetail().get(0);
                // ProductMapper로 Product 조회 후 세팅
                firstDetail.setProduct(productMapper.findById(firstDetail.getProductNo()));
                order.setProduct(firstDetail.getProduct());
            }
        }

        return orders;
    }
    
    //
    
    // 점주별 진행 중 주문 조회 (STATUS 0~2)
    public List<OrderHistoryDTO> getShopOrderHistory(int shopNo) {
        return orderRepository.findByShopShopNoAndStatusInOrderByOrderTimeOrderTimeNoDesc(shopNo, List.of(0, 1, 2));
    }

    // 배송 내역 조회 (STATUS 2~4)
    public List<OrderHistoryDTO> getDeliveryOrders(int shopNo) {
        return orderRepository.findByShopShopNoAndStatusInOrderByOrderTimeOrderTimeNoDesc(shopNo, List.of(2, 3, 4));
    }
    
    //	   
    //public List<OrderHistoryDTO> getDeliveryOrders(ShopDTO shop) { //,int status
   // return orderRepository.findByShopShopNoAndStatus(shop); //,status
}
    
