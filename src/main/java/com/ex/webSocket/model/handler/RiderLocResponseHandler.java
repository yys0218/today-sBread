package com.ex.webSocket.model.handler;

import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RiderLocResponseHandler implements WebSocketHandler {
    private final WebSocketResourcesManager manager;

    public RiderLocResponseHandler(WebSocketResourcesManager manager) {
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

        // 보내온 값 꺼내기
        // handlerType
        String handlerType = (String) map.get("handlerType");
        System.out.println(handlerType);
        // 좌표를 요청한 구매자 번호
        Integer buyerMemberNo = (Integer) map.get("buyerMemberNo");
        System.out.println(buyerMemberNo);
        // 좌표를 보낸 라이더 번호
        Integer riderMemberNo = (Integer) map.get("riderMemberNo");
        System.out.println(riderMemberNo);
        // 라이더의 좌표
        double lat = (double) map.get("lat");
        System.out.println(lat);
        double lng = (double) map.get("lng");
        System.out.println(lng);
        System.out.println(memberSessionList.containsKey(buyerMemberNo));
        System.out.println(memberSessionList);
        System.out.println(memberSessionList.keySet());
        if (memberSessionList.containsKey(buyerMemberNo)) {
            System.out.println("RiderLocResponseHandler 실행");
            WebSocketSession targetSession = memberSessionList.get(buyerMemberNo);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> reMap = new HashMap<String, Object>();
            reMap.put("handlerType", handlerType);
            reMap.put("lat", lat);
            reMap.put("lng", lng);
            reMap.put("riderMemberNo", riderMemberNo);
            reMap.put("buyerMemberNo", buyerMemberNo);

            String receiveMessage = mapper.writeValueAsString(reMap);
            targetSession.sendMessage(new TextMessage(receiveMessage));
        }

    }

}
