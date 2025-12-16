// package com.ex.webSocket.model.handler;

// // package com.ex.api.model.handler;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.CopyOnWriteArrayList;

// import org.springframework.stereotype.Component;
// import org.springframework.web.socket.CloseStatus;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketMessage;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.web.socket.handler.TextWebSocketHandler;

// import com.fasterxml.jackson.databind.ObjectMapper;

// @Component
// public class ProjectAlertHandler extends TextWebSocketHandler {

//     // 접속한 모든 회원의 세션을 담을 객체 (비로그인도 포함)
//     private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
//     // Map<WebSocketSession, memberNo> memberNoList
//     // 접속한 회원의 session을 가지고 memberNo를 특정하기 위해 사용
//     private final Map<WebSocketSession, Integer> memberNoList = new ConcurrentHashMap<>();
//     // Map<memberNo, WebSocketSession> memberSessionList
//     // 특정 회원의 memberNo를 사용하여 해당 유저의 session을 특정하기 위해 사용
//     private final Map<Integer, WebSocketSession> memberSessionList = new ConcurrentHashMap<>();
//     // Map<productNo , List<webSocketSession>> productList
//     // 특정 상품의 productNo를 사용하여 해당 상품을 보고있는 유저들의 session을 담을 객체 (비로그인도 포함)
//     private final Map<Integer, List<WebSocketSession>> productList = new ConcurrentHashMap<>();
//     // Map<productNo, Map<memberNo, WebSocketSession>> productMemberNoList
//     // 특정 상품의 productNo를 사용하여 해당 상품을 보고있는 로그인한 유저들의 session을 담을 객체 (로그인 한 유저만)
//     private final Map<Integer, Map<Integer, WebSocketSession>> productMemberNoMap = new ConcurrentHashMap<>();
//     // Map<productNo, Map<WebSocketSession, memberNo>> productSessionList
//     // 특정 상품의 productNo를 사용하여 해당 상품을 보고있는 로그인한 유저들의 session을 담을 객체 (로그인 한 유저만)
//     private final Map<Integer, Map<WebSocketSession, Integer>> productSessionMap = new ConcurrentHashMap<>();

//     @Override
//     public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//     }

//     @Override
//     // JavaScript 에서 websocket.send() 릍 통해 전달된 문자들을 서버에서 수신하여 처리하는 기능을 하는 메서드
//     // session - 현재 통신 중인 클라이언트와의 WebSocket 세션 정보
//     // message - 클라이언트가 전송한 실제 메시지(WebSocketMessage 객체)
//     public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//         System.out.println("웹소켓 접속 : " + message);
//         // Object 타입인 message를 getPayload를 사용하여 String 타입으로 변환
//         String strMessage = (String) message.getPayload();
//         // Jackson ObjectMapper를 이용해 JSON 문자열을 Map으로 변환한다.
//         // ObjectMapper 는 JSON ↔ Java 객체 변환하는데 쉽고 유용하다
//         // Map을 사용하는 이유는 JSON 은 Map 처럼 (Key:Value) 형식으로 사용하기 때문
//         ObjectMapper mapper = new ObjectMapper();
//         HashMap<String, Object> map = mapper.readValue(strMessage, HashMap.class);

//         // 전달 받은 값 꺼내기
//         // javascript에서 보낸 메세지 중 key값이 type인 값 꺼내기
//         String type = (map.get("type") instanceof String) ? (String) map.get("type")
//                 : "";

//         // javascript에서 보낸 메세지 중 key값이 memberNo인 값 꺼내기
//         int memberNo = 0;
//         Object memberNoObj = map.get("memberNo");
//         if (memberNoObj instanceof Number) {
//             memberNo = ((Number) memberNoObj).intValue();
//         }

//         // javascript에서 보낸 메세지 중 key값이 productNo인 값 꺼내기
//         int productNo = 0;
//         Object productNoObj = map.get("productNo");
//         if (productNoObj instanceof Number) {
//             productNo = ((Number) productNoObj).intValue();
//         }

//         // javascript에서 보낸 메세지 중 key값이 message인 값 꺼내기
//         String textMessage = "";
//         Object messageObj = map.get("message");
//         if (messageObj instanceof String) {
//             textMessage = (String) messageObj;
//         }

//         // 콘솔 출력용
//         System.out.println("websocket type : " + type);
//         System.out.println("websocket memberNo : " + memberNo);
//         System.out.println("websocket message : " + textMessage);
//         // 회원이 로그인 했을 경우 해당 memberNo 와 session을 session에 저장
//         // session 111 123 125 124 127 128 130 169
//         // memberNo 1 2 3 4 5 6 7 8
//         // type의 값이 "login" 인 경우 코드 실행
//         if (type.equals("login")) {
//             sessions.add(session);
//             System.out.println("websocket sessions : " + sessions);
//             memberNoList.put(session, memberNo);
//             System.out.println("websocket memberNoList : " + memberNoList);
//             memberSessionList.put(memberNo, session);
//             System.out.println("websocket memberSessionList : " + memberSessionList);
//         } else if (type.equals("productLogin")) { // 상품페이지를 보고 있는 회원의 정보를 저장
//             // productNo 키에 해당하는 리스트를 꺼낸다
//             List<WebSocketSession> sessions = productList.get(productNo);
//             // 없으면 새 리스트를 만들어 넣는다
//             if (sessions == null) {
//                 // new CopyOnWriteArrayList<>() : 여러 스레드가 동시에 읽고/쓰는 환경에서도 안전하게 동작
//                 sessions = new CopyOnWriteArrayList<>();
//                 productList.put(productNo, sessions);
//             }
//             // 리스트에 세션을 추가한다
//             sessions.add(session);

//         } else if (type.equals("boardAlert")) { // 회원의 문의/신고 글에 답변 작성 시 회원에게 알림.
//             System.out.println("WebSocket boardAlert 실행");
//             for (WebSocketSession connected : sessions) {
//                 WebSocketSession target = memberSessionList.get(memberNo);
//                 if (connected.equals(target)) {
//                     System.out.println(" Websocket : 회원 찾음");
//                     Map<String, Object> reMap = new HashMap<String, Object>();
//                     reMap.put("type", type); // 응답 타입
//                     // 안전하게 JSON 문자열 생성
//                     String reciveMessage = mapper.writeValueAsString(reMap);
//                     connected.sendMessage(new TextMessage(reciveMessage));
//                 }
//             }
//         } else if (type.equals("bid")) { // 상품의 금액이 변동 되었을 때 알림
//             // 해당 상품에 입찰한 사람들에게 알림을 보내줌
//             System.out.println("WebSocket bid 실행");
//             // Database에서 해당 상품에 입찰한 유저들의 memberNo를 조회해서 가져옴
//             ArrayList<Integer> memberList = productService.webSocketProductNoAll(productNo);
//             for (WebSocketSession connected : sessions) {
//                 for (int member : memberList) {
//                     WebSocketSession target = memberSessionMap.get(member);
//                     if (connected.equals(target) && !connected.equals(session)) {
//                         System.out.println(" Websocket bid : 회원 찾음");
//                         Map<String, Object> reMap = new HashMap<String, Object>();
//                         reMap.put("type", type); // 응답 타입
//                         // 안전하게 JSON 문자열 생성
//                         String reciveMessage = mapper.writeValueAsString(reMap);
//                         connected.sendMessage(new TextMessage(reciveMessage));
//                     }
//                 }
//             }

//         } else if (type.equals("productPrice")) { // 상품 content 에서 해당 상품의 가격이 변동
//             // 해당 페이지를 보고있는 사람들에게 상품의 가격이 변동되었다는걸 알림.
//             System.out.println("WebSocket productPrice 실행");
//             // 해당 productNo를 보고있는 사람들의 세션
//             List<WebSocketSession> list = productMemberNo.get(productNo);
//             for (WebSocketSession connected : list) {
//                 System.out.println(" Websocket ProductPrice : 회원 찾음");
//                 Map<String, Object> reMap = new HashMap<String, Object>();
//                 reMap.put("type", type); // 응답 타입
//                 // 안전하게 JSON 문자열 생성
//                 String reciveMessage = mapper.writeValueAsString(reMap);
//                 connected.sendMessage(new TextMessage(reciveMessage));
//             }

//         } else if (type.equals("listPrice")) { // 상품 list 페이지에서 해당 상품의 가격을 변동
//             // 해당 list 페이지를 보고있는 사람들에게 상품의 가격이 변동되었다는 걸 알림
//             System.out.println(" Websocket listPrice");
//             for (WebSocketSession connected : productListSessions) {
//                 Map<String, Object> reMap = new HashMap<String, Object>();
//                 reMap.put("type", type); // 응답 타입
//                 reMap.put("productNo", productNo);
//                 // 안전하게 JSON 문자열 생성
//                 String reciveMessage = mapper.writeValueAsString(reMap);
//                 System.out.println("listPrice 전송");
//                 connected.sendMessage(new TextMessage(reciveMessage));
//             }
//         }

//     }

//     @Override
//     public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//         int memberNo = memberNoList.get(session);
//         sessions.remove(session);
//         memberNoList.remove(session);
//         memberSessionList.remove(memberNo);
//         System.out.println("WebSocket 종료");

//     }
// }
