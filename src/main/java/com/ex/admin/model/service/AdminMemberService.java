package com.ex.admin.model.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.ex.member.model.data.MemberDTO;
import com.ex.admin.model.repository.AdminMemberRepository;
import com.ex.admin.model.repository.AdminShopRepository;
import com.ex.admin.model.repository.RestrictRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final AdminMemberRepository adminMemberRepository;
    private final AdminShopRepository adminShopRepository;
    private final RestrictRepository restrictRepository;

    /**
     * 회원 목록 조회 (검색 + 정렬 + 페이징)
     */
    public Page<MemberDTO> getMemberList(String keyword, Integer roleValue, String sortField, String sortDir, int page,
            int size) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        // keyword null-safe 처리
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        return adminMemberRepository.searchMembers(roleValue, searchKeyword, pageable);
    }

    /**
     * 특정 회원 조회
     */
    public Optional<MemberDTO> getMember(int memberNo) {
        return adminMemberRepository.findById(memberNo);
    }

    // // 전체 통계 시작
    // public Map<String, Object> getMemberStats(String period) {
    //     List<Object[]> rows;

    //     if ("week".equals(period)) {
    //         rows = adminMemberRepository.countNewMembersByWeek();
    //     } else if ("month".equals(period)) {
    //         rows = adminMemberRepository.countNewMembersByMonth();
    //     } else { // 기본 3개월 (주단위)
    //         rows = adminMemberRepository.countNewMembersByWeek();
    //     }

    //     List<String> labels = new ArrayList<>();
    //     List<Long> data = new ArrayList<>();

    //     DateTimeFormatter fmt;
    //     if ("month".equals(period)) {
    //         fmt = DateTimeFormatter.ofPattern("yyyy/MM");
    //     } else {
    //         fmt = DateTimeFormatter.ofPattern("MM/dd");
    //     }

    //     for (Object[] row : rows) {
    //         java.sql.Timestamp ts = (java.sql.Timestamp) row[0];
    //         LocalDate date = ts.toLocalDateTime().toLocalDate();
    //         labels.add(date.format(fmt));
    //         data.add(((Number) row[1]).longValue());
    //     }

    //     Map<String, Object> map = new HashMap<>();
    //     map.put("labels", labels);
    //     map.put("data", data);
    //     return map;
    // }

}
