package com.ex.webSocket.model.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.ex.webSocket.controller.WebSocketController;

import lombok.RequiredArgsConstructor;

// 설정에 관한 클래스 어노테이션
@Configuration
// 웹소켓 기능을 활성화 하는 어노테이션
@EnableWebSocket
@RequiredArgsConstructor // ChatHAndler 객체 자동생성
// 웹소켓 설정
// WebSocketConfigurer 인터페이스 구현필수!
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketController controller;

    /*
     * WebSocketConfigurer
     * - 웹소켓 핸들러 클래스를 등록할 수 있게 하는 인터페이스
     * - 특정 URL을 등록하는 역할
     * - 프토로콜이 다르다 . ws:// 기반 통신 (http:// 통신이 아님)
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(controller, "/ws/alert") // ws://localhost:8080/ws/alert 접속 URL 설정
                .setAllowedOrigins("*"); // 모든 요청 허용
        ;
    }
}