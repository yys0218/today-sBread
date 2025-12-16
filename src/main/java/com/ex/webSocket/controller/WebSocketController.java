package com.ex.webSocket.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.ex.webSocket.model.handler.WebSocketHandler;
import com.ex.webSocket.model.handler.WebSocketResourcesManager;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WebSocketController extends TextWebSocketHandler {

    private final WebSocketResourcesManager manager;
    private final Map<String, WebSocketHandler> handlers;

    // Spring이 같은 타입(WebSocketHandler)의 빈을 Map으로 묶어서 주입
    public WebSocketController(WebSocketResourcesManager manager,
            Map<String, WebSocketHandler> handlers) {
        this.manager = manager;
        this.handlers = handlers; // 이름이 Bean 이름으로 들어감
    }

    // public WebSocketController(WebSocketResourcesManager manager,
    // LoginHandler loginHandler,
    // AlertHandler alertHandler) {
    // this.manager = manager;
    // handlers.put("login", loginHandler);
    // handlers.put("alert", alertHandler);
    // }
    // 생성자에 수동으로 추가하게 되면 생성자 추가가 끝이 없음.

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // WebSocketMessage 객체에서 실제 전송된 데이터(payload) 를 추출하는 메서드
        // 해당 메서드는 Object 타입이므로 String 타입으로 변환 (다운캐스팅)
        String strMessage = (String) message.getPayload();
        // ObjectMapper : JSON 문자열 ↔ Java 객체로 변환하기 위해 사용
        ObjectMapper mapper = new ObjectMapper();
        // ObjectMapper를 이용해 JSON 형식의 문자열(strMessage)을 Java 객체로 변환하는 메서드
        // Message 는 JSON 형태 (Key : Value) 이므로 Java에 HashMap 객체로 저장
        Map<String, Object> map = mapper.readValue(strMessage, HashMap.class);

        // type 추출
        String handlerType = "";
        // map에서 key가 "type"인 value를 가져와서 타입이 String인지 확인
        // String 타입일 경우 안전하게 캐스팅하여 type 변수에 대입
        if (map.get("handlerType") instanceof String) {
            handlerType = (String) map.get("handlerType");
        }

        // type에 따라 핸들러 위임
        // Map<String, WebSocketHandler> handlers
        // 해당 핸들러 맵에서 Key값이 type으로 보내면서 해당 타입에 핸들러로 이동.
        WebSocketHandler handler = handlers.get(handlerType);
        System.out.println(handlers);
        if (handler != null) {
            handler.handle(session, map);
        } else {
            System.out.println("알 수 없는 handlerType: " + handlerType);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        manager.removeSession(session);
        System.out.println("WebSocket 종료");
    }
}