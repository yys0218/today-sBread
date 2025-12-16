package com.ex.webSocket.model.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.ex.alert.model.service.AlertHistoryService;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RiderAssignHandler implements WebSocketHandler {

    private final WebSocketResourcesManager manager;
    private final OrderHistoryRepository orderHistoryRepository;
    private final AlertHistoryService alertHistoryService;

    public RiderAssignHandler(WebSocketResourcesManager manager, OrderHistoryRepository orderHistoryRepository,
            AlertHistoryService alertHistoryService) {
        this.manager = manager;
        this.orderHistoryRepository = orderHistoryRepository;
        this.alertHistoryService = alertHistoryService;
    }

    @Override
    public void handle(WebSocketSession session, Map<String, Object> map) throws Exception {

        System.out.println("RiderAssignHandler 실행");

        // 일반유저(구매자) 세션 리스트
        Map<Integer, WebSocketSession> memberSessionList = this.manager.getMemberSessionList();
        // 상점유저(판매자) 세션 리스트
        Map<Integer, WebSocketSession> shopSessionList = this.manager.getShopSessionList();
        // 회원 번호 세션 리스트
        Map<WebSocketSession, Integer> memberNoList = this.manager.getMemberNoList();

        // 내 회원 번호 꺼내기
        Integer myMemberNo = memberNoList.get(session);

        // 상품 번호 꺼내기
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
        alertHistoryService.RiderAssignAlert(
                (String) map.get("handlerType"),
                myMemberNo, toMemberNo, dto);

        // 2. 매장한테 갈 알림 기록 save
        alertHistoryService.RiderAssignAlert((String) map.get("handlerType"),
                myMemberNo, toShopMemberNo, dto);

        // 받는사람에게 전해줄 JSON 타입 문자열 만들기
        ObjectMapper mapper = new ObjectMapper();

        // 구매자한테 알람 보내기
        if (memberSessionList.containsKey(toMemberNo)) {
            System.out.println("member Websocket 전송 실행");
            Map<String, Object> reMap = new HashMap<String, Object>();
            WebSocketSession targetSession = memberSessionList.get(toMemberNo);
            reMap.put("handlerType", map.get("handlerType"));
            reMap.put("memberType", "member");
            reMap.put("orderNo", orderNo);
            String receiveMessage = mapper.writeValueAsString(reMap);
            targetSession.sendMessage(new TextMessage(receiveMessage));
        }

        if (shopSessionList.containsKey(toShopMemberNo)) {
            System.out.println("shop Websocket 전송 실행");
            Map<String, Object> reMap = new HashMap<String, Object>();
            WebSocketSession targetSession = shopSessionList.get(toShopMemberNo);
            reMap.put("handlerType", map.get("handlerType"));
            reMap.put("memberType", "shop");
            reMap.put("orderNo", orderNo);
            String receiveMessage = mapper.writeValueAsString(reMap);
            targetSession.sendMessage(new TextMessage(receiveMessage));
        }

    }

}
