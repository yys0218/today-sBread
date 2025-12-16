package com.ex.webSocket.model.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RiderLocRequestHandler implements WebSocketHandler {

    private final WebSocketResourcesManager manager;

    public RiderLocRequestHandler(WebSocketResourcesManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(WebSocketSession session, Map<String, Object> map) throws Exception {
        // 세션 리스트 꺼내기
        // 일반유저(구매자) 세션 리스트
        Map<Integer, WebSocketSession> memberSessionList = this.manager.getMemberSessionList();
        // 라이더유저 세션 리스트
        Map<Integer, WebSocketSession> riderSessionList = this.manager.getRiderSessionList();
        // 상점유저(판매자) 세션 리스트
        Map<Integer, WebSocketSession> shopSessionList = this.manager.getShopSessionList();
        // 회원 번호 세션 리스트
        Map<WebSocketSession, Integer> memberNoList = this.manager.getMemberNoList();

        // 라이더 좌표를 받을 멤버 번호
        Integer buyerMemberNo = (Integer) map.get("buyerMemberNo");
        // 좌표를 보내줘야하는 멤버 번호
        Integer riderMemberNo = (Integer) map.get("riderMemberNo");

        if (riderSessionList.containsKey(riderMemberNo)) {
            WebSocketSession targetSession = riderSessionList.get(riderMemberNo);
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> reMap = new HashMap<String, Object>();

            reMap.put("handlerType", "riderLocRequestHandler");
            reMap.put("buyerMemberNo", buyerMemberNo);
            reMap.put("riderMemberNo", riderMemberNo);

            String receiveMessage = mapper.writeValueAsString(reMap);

            targetSession.sendMessage(new TextMessage(receiveMessage));

        }

    }

}
