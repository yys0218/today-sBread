package com.ex.webSocket.model.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.ex.alert.model.service.AlertHistoryService;
import com.ex.order.model.data.OrderDetailDTO;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class RiderPickupHandler implements WebSocketHandler {

    private final WebSocketResourcesManager manager;
    private final OrderHistoryRepository orderHistoryRepository;
    private final AlertHistoryService alertHistoryService;

    public RiderPickupHandler(WebSocketResourcesManager manager, OrderHistoryRepository orderHistoryRepository,
            AlertHistoryService alertHistoryService) {
        this.manager = manager;
        this.orderHistoryRepository = orderHistoryRepository;
        this.alertHistoryService = alertHistoryService;
    }

    @Override
    public void handle(WebSocketSession session, Map<String, Object> map) throws Exception {
        // 세션 리스트 꺼내기
        // 일반유저(구매자) 세션 리스트
        Map<Integer, WebSocketSession> memberSessionList = this.manager.getMemberSessionList();
        // 상점유저(판매자) 세션 리스트
        Map<Integer, WebSocketSession> shopSessionList = this.manager.getShopSessionList();
        // 회원 번호 세션 리스트
        Map<WebSocketSession, Integer> memberNoList = this.manager.getMemberNoList();

        // WebSocket으로 보낸 데이터 꺼내기
        String handlerType = (String) map.get("handlerType");
        Integer memberNo = (Integer) map.get("fromMemberNo");
        Integer orderNo = (Integer) map.get("orderNo");

        // OrderHistoryDTO 객체 가져오기 (shop의 번호 , member의 번호 등 다 꺼내올 수 있음)
        Optional<OrderHistoryDTO> _order = orderHistoryRepository.findByOrderNo(orderNo);
        // 조회된 결과값이 있다면 객체 꺼내기
        if (_order.isEmpty()) {
            System.out.println("주문번호 " + orderNo + "에 해당하는 주문이 없습니다.");
            return; // 더 진행하지 않음
        }
        OrderHistoryDTO dto = _order.get();
        // 알림받는 판매자,구매자 번호 가져오기
        Integer toMemberNo = dto.getMember().getMemberNo();
        Integer toShopMemberNo = dto.getShop().getMemberNo();

        // 1. 구매자한테 갈 알림 기록 save
        alertHistoryService.RiderAssignAlert(handlerType, memberNo, toMemberNo, dto);

        // 2. 매장한테 갈 알림 기록 save
        alertHistoryService.RiderAssignAlert(handlerType, memberNo, toShopMemberNo, dto);

        ObjectMapper mapper = new ObjectMapper();

        if (memberSessionList.containsKey(toMemberNo)) {
            WebSocketSession targetSession = memberSessionList.get(toMemberNo);
            Map<String, Object> reMap = new HashMap<String, Object>();

            reMap.put("handlerType", handlerType);
            reMap.put("orderNo", dto.getOrderNo());

            String receiveMessage = mapper.writeValueAsString(reMap);
            targetSession.sendMessage(new TextMessage(receiveMessage));
        }

    }

}
