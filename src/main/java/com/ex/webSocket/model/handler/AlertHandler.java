package com.ex.webSocket.model.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AlertHandler implements WebSocketHandler {

    private final WebSocketResourcesManager manager;

    public AlertHandler(WebSocketResourcesManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(WebSocketSession session, Map<String, Object> map) throws Exception {
        System.out.println("AlertHandler 실행");
        List<WebSocketSession> list = manager.getSessionList();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> reMap = new HashMap<String, Object>();
        reMap.put("handlerType", map.get("handlerType"));
        reMap.put("memberType", "member");
        String receiveMessage = mapper.writeValueAsString(reMap);
        for (WebSocketSession ws : list) {
            ws.sendMessage(new TextMessage(receiveMessage));

        }
    }

}
