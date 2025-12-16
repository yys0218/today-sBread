package com.ex.admin.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ex.DataNotFoundException;
import com.ex.admin.model.repository.AdminShopRepository;
import com.ex.shop.model.data.ShopDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminShopService {

    private final AdminShopRepository adminShopRepository;

    /**
     * 매장 목록 조회
     */
    public Page<ShopDTO> getShopList(String keyword, Integer roleValue, String sortField, String sortDir, int page, int size) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        // keyword null-safe 처리
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        return adminShopRepository.searchShopList(roleValue, keyword, pageable);
    }

    /**
     * 입점신청 목록 조회
     */
    public Page<ShopDTO> getRegistryList(String result, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "shopRegAt"));
        if (result == null || result.isEmpty()) {
            // 전체
            return adminShopRepository.findAll(pageable);
        }
        if ("A".equals(result)) {
            // 미답변 (아직 승인/거절/철회 처리 안된 경우)
            return adminShopRepository.findByShopRegResultIsNull(pageable);
        }
        return adminShopRepository.findByShopRegResult(result, pageable);
    }

    /**
     * 매장 단건 조회
     */
    public Optional<ShopDTO> getShop(int shopNo) {
        return adminShopRepository.findById(shopNo);
    }

    /**
     * 매장 제재 (정지/폐점 처리)
     */
    @Transactional
    public void restrictShop(int shopNo, int reason, int periodDays) {
        ShopDTO shop = adminShopRepository.findById(shopNo)
                .orElseThrow(() -> new DataNotFoundException("매장을 찾을 수 없습니다. ID=" + shopNo));

        // 운영 정지 = 1 로 상태 변경
        shop.setShopStatus(1); // 정지
        shop.setClosingReason(reason);
        // 필요하다면 closingAt, closingReasonDetail도 여기서 설정 가능
        adminShopRepository.save(shop);
    }

    /**
     * 매장 폐점 현황
     */

    public Page<ShopDTO> getClosedShopList(Pageable pageable) {
        return adminShopRepository.findByShopStatus(pageable);

    }

    // 공통 변환 메서드
    private Map<String, Object> toMap(List<Object[]> result, Function<Object, String> labelMapper) {
        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (Object[] row : result) {
            labels.add(labelMapper.apply(row[0]));
            if (row[1] != null) {
                data.add(Long.parseLong(row[1].toString()));
            } else {
                data.add(0L);
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("labels", labels);
        map.put("data", data);
        return map;
    }

    // // 입점 신청 결과
    // public Map<String, Object> getShopRegResult() {
    //     return toMap(adminShopRepository.findShopRegResult(),
    //             obj -> {
    //                 if (obj == null)
    //                     return "심사대기"; // null 처리
    //                 return switch (String.valueOf(obj)) {
    //                     case "Y" -> "승인";
    //                     case "N" -> "거절";
    //                     case "C" -> "자진철회";
    //                     default -> "알수없음";
    //                 };
    //             });
    // }

    // // 상점 운영 상태
    // public Map<String, Object> getShopStatus() {
    //     return toMap(adminShopRepository.findShopStatus(),
    //             obj -> {
    //                 if (obj == null)
    //                     return "미지정";
    //                 int status = ((Number) obj).intValue();
    //                 return switch (status) {
    //                     case 0 -> "정상운영";
    //                     case 1 -> "정지";
    //                     case 2 -> "폐점";
    //                     default -> "알수없음";
    //                 };
    //             });
    // }

    // // 상점 지역 (시도)
    // public Map<String, Object> getShopRegion() {
    //     return toMap(adminShopRepository.findShopRegion(),
    //             obj -> String.valueOf(obj)); // 그대로 "서울", "경기", ...
    // }

    // // 폐점 사유
    // public Map<String, Object> getShopClosingReason() {
    //     return toMap(adminShopRepository.findShopClosingReason(),
    //             obj -> {
    //                 if (obj == null)
    //                     return "알수없음";
    //                 int reason = ((Number) obj).intValue();
    //                 return switch (reason) {
    //                     case 0 -> "개인 사정";
    //                     case 1 -> "경영 문제";
    //                     case 2 -> "플랫폼 관련";
    //                     case 3 -> "기타";
    //                     default -> "알수없음";
    //                 };
    //             });
    // }

    // 가게명 중복 조회
    public boolean shopNameCheck(String shopName) {
        return adminShopRepository.findTopByShopNameAndShopRegResult(shopName, "Y").isPresent();
    }
    // 가게연락처 중복 조회
    public boolean shopContactCheck(String shopContact){
        return adminShopRepository.findTopByShopContactAndShopRegResult(shopContact, "Y").isPresent();
    }
}
