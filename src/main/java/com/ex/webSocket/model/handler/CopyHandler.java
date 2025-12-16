package com.ex.webSocket.model.handler;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class CopyHandler implements WebSocketHandler {

    // WebSocket에서 사용되는 각종 자원(세션, 회원번호, 상품별 매핑 등)을 한 곳에서 관리하는 클래스.
    private final WebSocketResourcesManager manager;

    public CopyHandler(WebSocketResourcesManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(WebSocketSession session, Map<String, Object> map) throws Exception {
        Integer memberNo = (Integer) map.get("memberNo");
        System.out.println("Handler 실행");
    }

}
