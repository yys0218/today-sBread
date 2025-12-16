
package com.ex.admin.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ex.admin.model.data.SettleConfigHistory;
import com.ex.admin.model.data.SettleDTO;
import com.ex.admin.model.repository.AdminMemberRepository;
import com.ex.admin.model.service.AdminMemberService;
import com.ex.admin.model.service.AdminSettleService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller

@RequiredArgsConstructor

@RequestMapping("/admin/site")
public class AdminSiteController {

    private final AdminSettleService adminSettleService;
    private final AdminMemberService adminMemberService;
    private final AdminMemberRepository adminMemberRepository;

    @GetMapping("/settle")
    public String adminSettle() {
        return "redirect:/admin/site/settle/list";
    }

    @GetMapping("/settle/list")
    public String settleList(@RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size,
            @RequestParam(defaultValue = "settleNo", name = "sortField") String sortField,
            @RequestParam(defaultValue = "DESC", name = "sortDir") String sortDir,
            @RequestParam(required = false, name = "keyword") String keyword, Model model) {

        // 현재 설정 가져오기
        SettleConfigHistory config = adminSettleService.getConfig();
        if (config == null) {
            config = new SettleConfigHistory();
            config.setShopRatio(0);
            config.setRiderRatio(0);
        }
        model.addAttribute("config", config);

        // 정산 설정 기록 가져오기
        Page<SettleConfigHistory> configHistory = adminSettleService.getConfigHistoryList(page, size);
        model.addAttribute("configHistory", configHistory);

        // 정산 내역 리스트 가져오기 (검색 + 정렬 적용)
        Page<SettleDTO> settleList = adminSettleService.getSettleList(page, size, sortField, sortDir, keyword);

        model.addAttribute("paging", settleList);
        model.addAttribute("settleList", settleList);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);

        return "admin/site/settle";
    }

    @PostMapping("/settle/config")
    public String settleConfig(@RequestParam("shopRatio") int shopRatio,

            @RequestParam("riderRatio") int riderRatio) {
        this.adminSettleService.updateConfig(shopRatio, riderRatio);
        return "redirect:/admin/site/settle/list";
    }

    // 차트
    @GetMapping("/settle/chart")
    public String settleChartCharge() {
        return "admin/site/settleChart";
    }

    // 사이트 통계 페이지
    @GetMapping("/statistics")
    public String siteStatisticsPage() {
        return "admin/site/siteChart";
    }

    // 통합 차트 데이터 API
    @GetMapping("/settle/chart/unified")
    @ResponseBody
    public Map<String, Object> getUnifiedChartData(
            @RequestParam("type") String type,
            @RequestParam(name = "memberNo", required = false) Integer memberNo,
            @RequestParam(name = "target", defaultValue = "all") String target) {

        LocalDate today = LocalDate.now();
        LocalDate endDate;
        LocalDate startDate;

        List<Object[]> revenueData;
        List<Object[]> expenseData;
        List<Object[]> salesData;

        // 대상 필터 설정
        Integer roleFilter = null;
        if ("seller".equals(target)) roleFilter = 1;
        else if ("rider".equals(target)) roleFilter = 4;

        // 기간 설정 및 데이터 조회 (endDate를 today로 변경)
        switch (type) {
            case "daily":
                endDate = today;
                startDate = today.minusDays(9);
                revenueData = adminSettleService.getDailyCharge(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                expenseData = adminSettleService.getDailyAmt(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                salesData = adminSettleService.getDailySales(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                break;
            case "weekly":
                endDate = today;
                startDate = today.minusWeeks(7);
                revenueData = adminSettleService.getWeeklyCharge(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                expenseData = adminSettleService.getWeeklyAmt(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                salesData = adminSettleService.getWeeklySales(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                break;
            case "monthly":
                endDate = today;
                startDate = today.minusMonths(11);
                revenueData = adminSettleService.getMonthlyCharge(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                expenseData = adminSettleService.getMonthlyAmt(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                salesData = adminSettleService.getMonthlySales(startDate.atStartOfDay(), endDate.atTime(23, 59, 59), memberNo, roleFilter);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 타입: " + type);
        }

        // 데이터 가공
        Map<String, Integer> revenueMap = new HashMap<>();
        for (Object[] row : revenueData) revenueMap.put((String) row[0], ((Number) row[1]).intValue());

        Map<String, Integer> expenseMap = new HashMap<>();
        for (Object[] row : expenseData) expenseMap.put((String) row[0], ((Number) row[1]).intValue());

        Map<String, Integer> salesMap = new HashMap<>();
        for (Object[] row : salesData) salesMap.put((String) row[0], ((Number) row[1]).intValue());

        List<String> labels = new ArrayList<>();
        List<Integer> revenueValues = new ArrayList<>();
        List<Integer> expenseValues = new ArrayList<>();
        List<Integer> salesValues = new ArrayList<>();

        // 기간별 라벨 및 데이터 채우기
        if ("daily".equals(type)) {
            for (int i = 0; i < 10; i++) {
                LocalDate d = startDate.plusDays(i);
                String key = d.toString();
                labels.add(key);
                revenueValues.add(revenueMap.getOrDefault(key, 0));
                expenseValues.add(expenseMap.getOrDefault(key, 0));
                salesValues.add(salesMap.getOrDefault(key, 0));
            }
        } else if ("weekly".equals(type)) {
            for (int i = 0; i < 8; i++) {
                LocalDate d = startDate.plusWeeks(i);
                String key = d.get(IsoFields.WEEK_BASED_YEAR) + "-" + String.format("%02d", d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
                labels.add(key);
                revenueValues.add(revenueMap.getOrDefault(key, 0));
                expenseValues.add(expenseMap.getOrDefault(key, 0));
                salesValues.add(salesMap.getOrDefault(key, 0));
            }
        } else if ("monthly".equals(type)) {
            for (int i = 0; i < 12; i++) {
                LocalDate d = startDate.plusMonths(i);
                String key = d.getYear() + "-" + String.format("%02d", d.getMonthValue());
                labels.add(key);
                revenueValues.add(revenueMap.getOrDefault(key, 0));
                expenseValues.add(expenseMap.getOrDefault(key, 0));
                salesValues.add(salesMap.getOrDefault(key, 0));
            }
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("labels", labels);
        response.put("revenue", revenueValues);
        response.put("expense", expenseValues);
        response.put("sales", salesValues);

        return response;
    }
}
