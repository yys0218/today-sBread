package com.ex;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

// 해당 클래스가 컨트롤러 역할을 하는 클래스임을 스프링에게 알려주는 어노테이션
@Controller
// 클라이언트가 "아이피:포트/test" (루트 경로)로 요청을 보낼 때
// 이 컨트롤러의 메서드(또는 클래스)에서 해당 요청을 처리하도록 매핑하는 어노테이션
@RequestMapping("/test/")
public class TestController {

    // 기초 테스트 메인 페이지 이동
    // 요청 URL (Get) : http://localhost:8080/test
    @GetMapping("main")
    public String getMethodName() {
        return new String("/test/main");
    }

    // 기초 alert 이동 기능
    // 요청 URL (Get) : http://localhost:8080/msg
    // 요청 파라미터 : String icon , String title , String msg , String loc
    @GetMapping("msg")
    public String alertMsg(@ModelAttribute("icon") String icon, @ModelAttribute("title") String title,
            @ModelAttribute("msg") String msg, @RequestParam("loc") String loc, Model model) {

        loc = "/test/main";
        model.addAttribute("loc", loc);

        return new String("/common/msg");
    }

}
