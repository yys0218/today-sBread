package com.ex.admin.model.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ex.admin.model.repository.AdminOrderRepository;
import com.ex.admin.model.repository.AdminMemberRepository;
import com.ex.admin.model.repository.AdminShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashService {
    // 여기는 대쉬보드용
    private final AdminOrderRepository adminOrderRepository;
    private final AdminMemberRepository adminMemberRepository;
    private final AdminShopRepository adminShopRepository;

    // 오늘 총 주문수
    public Map<String, Object> getTodayOrders() {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todayCount = adminOrderRepository.countCompletedOrdersToday(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        long yesterdayCount = adminOrderRepository.countCompletedOrdersToday(
                yesterday.atStartOfDay(), today.atStartOfDay());

        long diff = todayCount - yesterdayCount;
        double percent = (yesterdayCount == 0) ? (todayCount > 0 ? 100.0 : 0.0) : (double) diff / yesterdayCount * 100;

        result.put("todayCount", todayCount);
        result.put("yesterdayCount", yesterdayCount);
        result.put("diff", diff);
        result.put("percent", percent);

        return result;
    }

    // 이번주 총 주문수
    public Map<String, Long> getOrdersForThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Object[]> result = adminOrderRepository.countCompletedOrdersPerDay(start, end);
        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Object[] row : result) {
            chartData.put((String) row[0], ((Number) row[1]).longValue());
        }
        return chartData;
    }

    // 오늘 매출 합계
    public Map<String, Object> getTodaySales() {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        long todaySales = adminOrderRepository.sumCompletedOrderPriceToday(
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        long yesterdaySales = adminOrderRepository.sumCompletedOrderPriceToday(
                yesterday.atStartOfDay(), today.atStartOfDay());

        long diff = todaySales - yesterdaySales;
        double percent = (yesterdaySales == 0) ? (todaySales > 0 ? 100.0 : 0.0) : (double) diff / yesterdaySales * 100;

        result.put("todaySales", todaySales);
        result.put("yesterdaySales", yesterdaySales);
        result.put("diff", diff);
        result.put("percent", percent);

        return result;
    }

    // 이번 주 매출 추이 (일별 합계)
    public Map<String, Long> getSalesForThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDateTime start = startOfWeek.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Object[]> result = adminOrderRepository.sumCompletedOrderPricePerDay(start, end);
        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Object[] row : result) {
            chartData.put((String) row[0], ((Number) row[1]).longValue());
        }
        return chartData;
    }

    public Map<String, Object> getWeeklyNewMembers() {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();

        // 이번 주 (월요일 ~ 오늘)
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        long thisWeekCount = adminMemberRepository.countNewMembersBetween(
                thisWeekStart.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        // 지난 주 (지난주 월요일 ~ 일요일)
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);
        long lastWeekCount = adminMemberRepository.countNewMembersBetween(
                lastWeekStart.atStartOfDay(),
                lastWeekEnd.plusDays(1).atStartOfDay());

        // 증감 계산
        long diff = thisWeekCount - lastWeekCount;
        double percent = (lastWeekCount == 0) ? (thisWeekCount > 0 ? 100.0 : 0.0) : (double) diff / lastWeekCount * 100;

        result.put("thisWeekCount", thisWeekCount);
        result.put("lastWeekCount", lastWeekCount);
        result.put("diff", diff);
        result.put("percent", percent);

        // 이번 주 일자별 데이터 (차트용)
        List<Object[]> data = adminMemberRepository.countNewMembersPerDay(
                thisWeekStart.atStartOfDay(),
                today.plusDays(1).atStartOfDay());
        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Object[] row : data) {
            chartData.put((String) row[0], ((Number) row[1]).longValue());
        }
        result.put("chartLabels", chartData.keySet());
        result.put("chartData", chartData.values());

        return result;
    }

    public Map<String, Object> getWeeklyNewShops() {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);

        // 이번 주 신규 입점
        long thisWeekCount = adminShopRepository.countNewShopsBetween(
                thisWeekStart.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        // 지난 주 신규 입점
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);
        long lastWeekCount = adminShopRepository.countNewShopsBetween(
                lastWeekStart.atStartOfDay(),
                lastWeekEnd.plusDays(1).atStartOfDay());

        long diff = thisWeekCount - lastWeekCount;
        double percent = (lastWeekCount == 0) ? (thisWeekCount > 0 ? 100.0 : 0.0) : (double) diff / lastWeekCount * 100;

        result.put("thisWeekCount", thisWeekCount);
        result.put("lastWeekCount", lastWeekCount);
        result.put("diff", diff);
        result.put("percent", percent);

        // 이번 주 일자별 데이터 (차트용)
        List<Object[]> data = adminShopRepository.countNewShopsPerDay(
                thisWeekStart.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Object[] row : data) {
            chartData.put((String) row[0], ((Number) row[1]).longValue());
        }

        result.put("chartLabels", chartData.keySet());
        result.put("chartData", chartData.values());

        return result;
    }

    public Map<String, Object> getWeeklyClosedShops() {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);

        // 이번 주 폐점 건수
        long thisWeekCount = adminShopRepository.countClosedShopsBetween(
                thisWeekStart.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        // 지난 주 폐점 건수
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);
        long lastWeekCount = adminShopRepository.countClosedShopsBetween(
                lastWeekStart.atStartOfDay(),
                lastWeekEnd.plusDays(1).atStartOfDay());

        long diff = thisWeekCount - lastWeekCount;
        double percent = (lastWeekCount == 0) ? (thisWeekCount > 0 ? 100.0 : 0.0) : (double) diff / lastWeekCount * 100;

        result.put("thisWeekCount", thisWeekCount);
        result.put("lastWeekCount", lastWeekCount);
        result.put("diff", diff);
        result.put("percent", percent);

        // 일자별 폐점 건수 (차트용)
        List<Object[]> data = adminShopRepository.countClosedShopsPerDay(
                thisWeekStart.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Object[] row : data) {
            chartData.put((String) row[0], ((Number) row[1]).longValue());
        }

        result.put("chartLabels", chartData.keySet());
        result.put("chartData", chartData.values());

        return result;
    }
}
