package com.ex.webSocket.model.handler;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class RiderDeliveryHandler implements WebSocketHandler {

    private WebSocketResourcesManager manager;

    public RiderDeliveryHandler(WebSocketResourcesManager manager) {
        this.manager = manager;
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

    }

}
