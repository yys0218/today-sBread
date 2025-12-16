package com.ex.admin.model.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ex.admin.model.data.SettleConfigHistory;
import com.ex.admin.model.data.SettleDTO;
import com.ex.admin.model.repository.AdminOrderRepository;
import com.ex.admin.model.repository.AdminSettleConfigHistoryRepository;
import com.ex.admin.model.repository.AdminSettleRepository;

import lombok.RequiredArgsConstructor;

@Service

@RequiredArgsConstructor
public class AdminSettleService {

    private final AdminSettleRepository adminSettleRepository;
    private final AdminSettleConfigHistoryRepository adminSettleConfigHistoryRepository;
    private final AdminOrderRepository adminOrderRepository; // 의존성 추가

    public SettleConfigHistory getConfig() {
        return this.adminSettleConfigHistoryRepository.findTopByOrderByUpdatedAtDesc();
    }

    public Page<SettleConfigHistory> getConfigHistoryList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,
                "historyId"));
        return this.adminSettleConfigHistoryRepository.findAllByOrderByHistoryIdDesc(
                pageable);
    }

    public SettleConfigHistory updateConfig(int shopRatio, int riderRatio) {
        SettleConfigHistory sch = new SettleConfigHistory();
        sch.setShopRatio(shopRatio);
        sch.setRiderRatio(riderRatio);
        sch.setUpdatedAt(LocalDateTime.now());
        return this.adminSettleConfigHistoryRepository.save(sch);
    }

    // 검색 + 정렬된 정산 리스트
    public Page<SettleDTO> getSettleList(int page, int size, String sortField, String sortDir, String keyword) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page,
                size, sort);

        if (keyword != null && !keyword.isEmpty()) {
            return adminSettleRepository.findBySettleNameContainingIgnoreCase(keyword,
                    pageable);
        }
        return adminSettleRepository.findAll(pageable);
    }

    // 수익 (수수료)
    public List<Object[]> getDailyCharge(LocalDateTime start, LocalDateTime end, Integer memberNo, Integer roleFilter) {
        return adminSettleRepository.findDailyCharge(start, end, memberNo, roleFilter);
    }

    public List<Object[]> getWeeklyCharge(LocalDateTime start, LocalDateTime end, Integer memberNo,
            Integer roleFilter) {
        return adminSettleRepository.findWeeklyCharge(start, end, memberNo, roleFilter);
    }

    public List<Object[]> getMonthlyCharge(LocalDateTime start, LocalDateTime end, Integer memberNo,
            Integer roleFilter) {
        return adminSettleRepository.findMonthlyCharge(start, end, memberNo, roleFilter);
    }

    // 지출 (정산금)
    public List<Object[]> getDailyAmt(LocalDateTime start, LocalDateTime end, Integer memberNo, Integer roleFilter) {
        return adminSettleRepository.findDailyAmt(start, end, memberNo, roleFilter);
    }

    public List<Object[]> getWeeklyAmt(LocalDateTime start, LocalDateTime end, Integer memberNo, Integer roleFilter) {
        return adminSettleRepository.findWeeklyAmt(start, end, memberNo, roleFilter);
    }

    public List<Object[]> getMonthlyAmt(LocalDateTime start, LocalDateTime end, Integer memberNo, Integer roleFilter) {
        return adminSettleRepository.findMonthlyAmt(start, end, memberNo, roleFilter);
    }

    // 매출
    public List<Object[]> getDailySales(LocalDateTime start, LocalDateTime end, Integer memberNo, Integer roleFilter) {
        return adminOrderRepository.sumCompletedOrderPricePerDay(start, end);
    }

    public List<Object[]> getWeeklySales(LocalDateTime start, LocalDateTime end, Integer memberNo, Integer roleFilter) {
        return adminOrderRepository.sumCompletedOrderPricePerWeek(start, end);
    }

    public List<Object[]> getMonthlySales(LocalDateTime start, LocalDateTime end, Integer memberNo, Integer roleFilter) {
        return adminOrderRepository.sumCompletedOrderPricePerMonth(start, end);
    }

}