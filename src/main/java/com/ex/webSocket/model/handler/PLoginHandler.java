// package com.ex.webSocket.model.handler;

// import java.util.Map;

// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.WebSocketSession;

// /**
// * LoginHandler
// *
// * WebSocket 연결 시 클라이언트가 보낸 로그인 정보(memberNo)를 기반으로
// * 세션과 회원 번호를 매핑하고 관리하는 핸들러 클래스.
// */
// @Component
// public class PLoginHandler implements WebSocketHandler {

// // 세션, 회원번호 등의 WebSocket 관련 자원을 관리하는 매니저
// private final ExamWebSocketResourcesManager manager;

// /*
// * LoginHandler 생성자
// *
// * @param manager 세션 및 회원 정보를 관리하는 WebSocketResourcesManager
// */
// public PLoginHandler(ExamWebSocketResourcesManager manager) {
// this.manager = manager;
// }

// /**
// * 클라이언트로부터 로그인 관련 데이터를 받아 세션과 회원번호를 매핑한다.
// *
// * @param session 현재 WebSocket 연결 세션
// * @param map 클라이언트에서 전송한 데이터 (예: {"memberNo": 123})
// * @throws Exception 처리 중 발생할 수 있는 예외
// */
// @Override
// public void handle(WebSocketSession session, Map<String, Object> map) throws
// Exception {
// System.out.println("LoginHandler 실행");
// int memberNo = 0;

// // 전달받은 데이터에서 memberNo 추출
// Object memberNoObj = map.get("memberNo");
// // 추출한 memberNo가 Number 타입 이면 int 타입으로 변환
// if (memberNoObj instanceof Number) {
// memberNo = ((Number) memberNoObj).intValue();
// }

// // 유효한 회원번호가 있을 경우 자원 관리 매핑 수행
// if (memberNo != 0) {
// // 세션 리스트에 현재 세션이 없으면 추가
// if (!manager.getSessions().contains(session)) {
// manager.getSessions().add(session);
// }
// // 세션-회원번호 맵에 등록
// manager.getMemberNoList().put(session, memberNo);
// // 회원번호-세션 맵에 등록
// manager.getMemberSessionList().put(memberNo, session);
// }
// }
// }