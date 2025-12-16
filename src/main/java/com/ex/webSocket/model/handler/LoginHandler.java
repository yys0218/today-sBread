package com.ex.webSocket.model.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.service.ShopService;

@Component
public class LoginHandler implements WebSocketHandler {

    private final WebSocketResourcesManager manager;

    private final ShopService shopService;

    public LoginHandler(WebSocketResourcesManager manager, ShopService shopService) {
        this.manager = manager;
        this.shopService = shopService;
    }

    @Override
    public void handle(WebSocketSession session, Map<String, Object> map) throws Exception {
        System.out.println("LoginHandler 클래스 handle 메서드 실행");

        // 회원 번호 가져오기
        int memberNo = (int) map.get("fromMemberNo");

        List<WebSocketSession> list = manager.getSessionList();
        Map<WebSocketSession, Integer> memberNoList = manager.getMemberNoList();
        Map<Integer, WebSocketSession> memberSessionList = manager.getMemberSessionList();
        Map<Integer, WebSocketSession> shopSessionList = manager.getShopSessionList();
        if (!list.contains(session)) {
            list.add(session);
        }
        if (!memberNoList.containsKey(session)) {
            memberNoList.put(session, memberNo);
        }
        Optional<ShopDTO> _shop = this.shopService.optionalFindByMemberNo(memberNo);
        if (_shop.isPresent()) {
            if (!shopSessionList.containsKey(_shop.get().getMemberNo())) {
                shopSessionList.put(memberNo, session);
                System.out.println("shopSessionList");
            }
        } else {
            if (!memberSessionList.containsKey(memberNo)) {
                memberSessionList.put(memberNo, session);
                System.out.println("memberSessionList");
            }
        }

        System.out.println("manager sessionList에 session 주입");
    }

}
