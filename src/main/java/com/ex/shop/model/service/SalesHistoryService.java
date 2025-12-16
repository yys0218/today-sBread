package com.ex.shop.model.service;

import com.ex.shop.model.data.SalesHistoryDTO;
import com.ex.shop.model.repository.SalesHistoryRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SalesHistoryService
 * --------------------------------------------------------------------------------------------------------
 * 매출 내역(SalesHistory) 관련 비즈니스 로직 담당 서비스
 * - 전체 매출 조회
 * - 특정 상점 기준 매출 조회
 */
@Service
public class SalesHistoryService {

    private final SalesHistoryRepository repository;

    /**
     * 생성자 주입(Constructor Injection)
     */
    public SalesHistoryService(SalesHistoryRepository repository) {
        this.repository = repository;
    }

    // ======================= 전체 매출 조회 =======================

    /**
     * 전체 매출 내역 조회
     *
     * @return List<SalesHistoryDTO> 전체 매출 내역
     */
    public List<SalesHistoryDTO> getAllSales() {
        return repository.findAll();
    }

    // ======================= 상점 기준 매출 조회 =======================

    /**
     * 특정 상점(shopNo) 기준 매출 내역 조회
     *
     * @param shopNo 상점 번호
     * @return List<SalesHistoryDTO> 해당 상점의 매출 내역
     */
    public List<SalesHistoryDTO> getSalesByShop(Integer shopNo) {
        return repository.findByShopNo(shopNo);
    }
    
    //public List<SalesHistoryDTO> getSettlementHistory(int shopNo) {
    //    return repository.findSettlementHistory(shopNo);
    //}  
    
    //	정산 내역 조회 하기 
    @Transactional
    public List<SalesHistoryDTO> applySettlementAndGetHistory(int shopNo) {
        // 1. 내 상점에 해당하는 정산내역 조회
        List<SalesHistoryDTO> completedSales = repository.findByShopNoOrderByCreatedAtDesc(shopNo);

        // 2. 해당 상점(shopNo) 필터링
        
        completedSales = completedSales.stream()
                                       .filter(s -> s.getShopNo() == shopNo)
                                       .toList();

        // 3. 배송 완료 금액 합산
        int totalSales = completedSales.stream()
                                       .mapToInt(SalesHistoryDTO::getSalesAmount)
                                       .sum();

        // 4. 수수료 제외 후 정산 금액 계산
        int settleAmount = (int)(totalSales * 0.97); // 예: 3% 수수료 제외
        int balance = totalSales - settleAmount;

        // 5. 정산 내역 생성
        if (totalSales > 0) { // 정산할 금액이 있을 때만
            SalesHistoryDTO settlement = new SalesHistoryDTO();
            settlement.setShopNo(shopNo);
            settlement.setSalesType(3);      // 정산
            settlement.setSalesAmount(settleAmount);
            settlement.setSalesBalance(balance);
            settlement.setCreatedAt(LocalDateTime.now());
            repository.save(settlement);
        }

        // 6. 전체 내역 반환 (기존 매출 + 방금 만든 정산 내역)
        return repository.findByShopNoOrderByCreatedAtDesc(shopNo);
    }

}
