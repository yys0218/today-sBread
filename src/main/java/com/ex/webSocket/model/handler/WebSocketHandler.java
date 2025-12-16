package com.ex.webSocket.model.handler;

import java.util.Map;

import org.springframework.web.socket.WebSocketSession;

/*
 * WebSocketHandler 인터페이스
 * ----------------------------------------
 *  WebSocket 요청을 처리하기 위한 공통 인터페이스
 *  다양한 기능별 핸들러(Login, Chat, Notification 등)가 이 인터페이스를 구현
 *  각 기능별 처리를 WebSocketController의 handleMessage 안에서 if문으로 나누지 않고
 *  별도 핸들러로 분리함으로써 코드가 깔끔해지고 유지보수가 쉬워진다.
 *  handle 메서드를 통해 각 세션과 전송된 데이터를 기반으로 로직을 수행
 */
public interface WebSocketHandler {
    /**
     * 클라이언트로부터 전달받은 WebSocket 메시지를 처리하는 메서드
     *
     * @param session WebSocket 세션 (현재 연결된 사용자에 대한 세션 정보)
     * @param map     클라이언트에서 전송된 데이터(payload)를 Key-Value 형태로 파싱한 맵
     * @throws Exception 메시지 처리 중 발생할 수 있는 예외
     */
    void handle(WebSocketSession session, Map<String, Object> map) throws Exception;
}
