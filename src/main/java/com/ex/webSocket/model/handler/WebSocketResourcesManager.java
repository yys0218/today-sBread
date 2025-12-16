package com.ex.webSocket.model.handler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import lombok.Getter;

/**
 * WebSocketResourcesManager
 *
 * WebSocket에서 사용되는 각종 자원(세션, 회원번호, 상품별 매핑 등)을 한 곳에서 관리하는 클래스.
 * 여러 스레드에서 동시에 접근하더라도 안전하도록
 * CopyOnWriteArrayList와 ConcurrentHashMap을 사용한다.
 */
@Component
@Getter
public class WebSocketResourcesManager {

    /**
     * 현재 연결된 모든 회원의 WebSocket 세션 리스트
     *
     * <p>
     * 클라이언트(회원)가 웹소켓에 연결될 때마다 해당 WebSocketSession을 저장합니다.
     * </p>
     *
     * <p>
     * - 자료구조 : CopyOnWriteArrayList (멀티스레드 환경에서 안전하게 접근 가능) <br>
     * - 저장 방식 : 단순히 WebSocketSession 객체를 순차적으로 보관 <br>
     * - 용도 : 브로드캐스트(전체 알림) 전송 시 활용
     * </p>
     */
    private final List<WebSocketSession> sessionList = new CopyOnWriteArrayList<>();
    /**
     * 회원번호 세션 관리 맵
     * <p>
     * 회원의 WebSocketSession을 키(key)로 하고, 해당 회원 번호를 값(value)으로 저장합니다.
     * </p>
     * <ul>
     * <li>Key : WebSocketSession 객체</li>
     * <li>Value : 회원 번호 (Integer)</li>
     * </ul>
     */
    private final Map<WebSocketSession, Integer> memberNoList = new ConcurrentHashMap<>();

    /**
     * 라이더 전용 세션 관리 맵
     * <p>
     * 회원 번호를 키(key)로 하고, 해당 라이더의 WebSocketSession을 값(value)으로 저장합니다.
     * </p>
     * <ul>
     * <li>Key : 회원 번호 (Integer)</li>
     * <li>Value : WebSocketSession 객체</li>
     * </ul>
     * <p>
     * ConcurrentHashMap을 사용하여 멀티스레드 환경(다수 라이더 동시 접속)에서도 안전하게 접근할 수 있습니다.
     * </p>
     */
    private final Map<Integer, WebSocketSession> riderSessionList = new ConcurrentHashMap<>();

    /**
     * 구매유저 전용 세션 관리 맵
     * <p>
     * 회원 번호를 키(key)로 하고, 해당 구매유저의 WebSocketSession을 값(value)으로 저장합니다.
     * </p>
     * <ul>
     * <li>Key : 회원 번호 (Integer)</li>
     * <li>Value : WebSocketSession 객체</li>
     * </ul>
     * <p>
     * ConcurrentHashMap을 사용하여 멀티스레드 환경(다수 구매유저 동시 접속)에서도 안전하게 접근할 수 있습니다.
     * </p>
     */
    private final Map<Integer, WebSocketSession> memberSessionList = new ConcurrentHashMap<>();

    /**
     * 매점유저 전용 세션 관리 맵
     * <p>
     * 회원 번호를 키(key)로 하고, 해당 매점유저의 WebSocketSession을 값(value)으로 저장합니다.
     * </p>
     * <ul>
     * <li>Key : 회원 번호 (Integer)</li>
     * <li>Value : WebSocketSession 객체</li>
     * </ul>
     * <p>
     * ConcurrentHashMap을 사용하여 멀티스레드 환경(다수 매점유저 동시 접속)에서도 안전하게 접근할 수 있습니다.
     * </p>
     */
    private final Map<Integer, WebSocketSession> shopSessionList = new ConcurrentHashMap<>();

    // 해당 세션 목록에서 session을 지우는 메서드
    public void removeSession(WebSocketSession session) {
        System.out.println("removeSession 메서드 실행");

        // 세션 - 회원번호 매핑에서 밸류로 저장된 회원 정보를 꺼내면서 해당 항목을 삭제
        // Key(WebsocketSession)에 해당하는 Value(Integer memberNo)가 리턴됨.
        Integer memberNo = memberNoList.remove(session);

        // 전체 세션 리스트에서 제거
        sessionList.remove(session);

        // 회원 번호를 가지고 모든 세션리스트에서 제거
        // Map객체는 remove(Key) 메서드는 Key가 존재하지 않으면 아무일도 하지않고 null을 반환 (예외처리 안해도 됌)
        if (memberNo != null) {
            memberSessionList.remove(memberNo);
            riderSessionList.remove(memberNo);
            shopSessionList.remove(memberNo);
        }

    }
}