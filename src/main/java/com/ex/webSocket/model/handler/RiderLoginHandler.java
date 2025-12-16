package com.ex.webSocket.model.handler;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class RiderLoginHandler implements WebSocketHandler {

    // WebSocket에서 사용되는 각종 자원(세션, 회원번호, 상품별 매핑 등)을 한 곳에서 관리하는 클래스.
    private final WebSocketResourcesManager manager;

    public RiderLoginHandler(WebSocketResourcesManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(WebSocketSession session, Map<String, Object> map) throws Exception {
        System.out.println("RiderLoginHandler 실행");
        // 보내는 사람 회원 번호 map에서 꺼내기
        Integer fromMemberNo = (Integer) map.get("fromMemberNo");
        //
        List<WebSocketSession> list = this.manager.getSessionList();
        Map<WebSocketSession, Integer> memberNoList = this.manager.getMemberNoList();
        Map<Integer, WebSocketSession> riderSessionList = this.manager.getRiderSessionList();

        if (!memberNoList.containsKey(session)) {
            memberNoList.put(session, fromMemberNo);
        }

        if (!list.contains(session)) {
            list.add(session);
        }

        if (!riderSessionList.containsKey(fromMemberNo)) {
            riderSessionList.put(fromMemberNo, session);
        }
        System.out.println(session);
        System.out.println(memberNoList);
        System.out.println(riderSessionList);

        System.out.println("RiderLoginHandler 종료");
    }

}
