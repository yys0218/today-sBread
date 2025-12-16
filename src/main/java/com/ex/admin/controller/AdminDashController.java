package com.ex.admin.controller;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ex.admin.model.service.AdminDashService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/main")
public class AdminDashController {

    private final AdminDashService adminDashService;

    @GetMapping("/dashBoard")
    public String dashboard(Model model) {

        // 오늘 주문 수
        Map<String, Object> orders = adminDashService.getTodayOrders();
        model.addAttribute("orders", orders);

        // 주간 주문 차트
        Map<String, Long> ordersForThisWeek = adminDashService.getOrdersForThisWeek();
        if (ordersForThisWeek.isEmpty()) {
            model.addAttribute("orderLabels", List.of("월", "화", "수", "목", "금", "토", "일"));
            model.addAttribute("orderData", List.of(5L, 8L, 3L, 6L, 7L, 10L, 12L));
        } else {
            model.addAttribute("orderLabels", ordersForThisWeek.keySet());
            model.addAttribute("orderData", ordersForThisWeek.values());
        }

        // 오늘 매출
        Map<String, Object> sales = adminDashService.getTodaySales();
        model.addAttribute("sales", sales);

        // 주간 매출 차트
        Map<String, Long> salesForThisWeek = adminDashService.getSalesForThisWeek();
        if (salesForThisWeek.isEmpty()) {
            model.addAttribute("salesLabels", List.of("월", "화", "수", "목", "금", "토", "일"));
            model.addAttribute("salesData", List.of(120000L, 190000L, 80000L, 150000L, 200000L, 300000L, 450000L));
        } else {
            model.addAttribute("salesLabels", salesForThisWeek.keySet());
            model.addAttribute("salesData", salesForThisWeek.values());
        }

        // 신규 회원
        Map<String, Object> members = adminDashService.getWeeklyNewMembers();
        if (((Collection<?>) members.get("chartLabels")).isEmpty()) {
            members.put("thisWeekCount", 15L);
            members.put("lastWeekCount", 10L);
            members.put("diff", 5L);
            members.put("percent", 50.0);
            members.put("chartLabels", List.of("월", "화", "수", "목", "금", "토", "일"));
            members.put("chartData", List.of(2L, 3L, 1L, 4L, 0L, 5L, 0L));
        }
        model.addAttribute("members", members);

        // 신규 입점
        Map<String, Object> shops = adminDashService.getWeeklyNewShops();
        if (((Collection<?>) shops.get("chartLabels")).isEmpty()) {
            shops.put("thisWeekCount", 3L);
            shops.put("lastWeekCount", 4L);
            shops.put("diff", -1L);
            shops.put("percent", -25.0);
            shops.put("chartLabels", List.of("월", "화", "수", "목", "금", "토", "일"));
            shops.put("chartData", List.of(1L, 0L, 1L, 0L, 1L, 0L, 0L));
        }
        model.addAttribute("newShops", shops);

        // 폐점
        Map<String, Object> closed = adminDashService.getWeeklyClosedShops();
        if (((Collection<?>) closed.get("chartLabels")).isEmpty()) {
            closed.put("thisWeekCount", 1L);
            closed.put("lastWeekCount", 0L);
            closed.put("diff", 1L);
            closed.put("percent", 100.0);
            closed.put("chartLabels", List.of("월", "화", "수", "목", "금", "토", "일"));
            closed.put("chartData", List.of(0L, 0L, 0L, 1L, 0L, 0L, 0L));
        }
        model.addAttribute("closedShops", closed);

        return "admin/main";
    }
}
