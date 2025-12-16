package com.ex.alert.model.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ex.alert.model.data.AlertHistoryDTO;
import com.ex.alert.model.repository.AlertHistoryRepository;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.member.model.service.MemberService;
import com.ex.order.model.data.OrderHistoryDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlertHistoryService {

    private final AlertHistoryRepository alertHistoryRepository;

    private final MemberRepository memberRepository;

    /**
     * 
     * @param alertType    (Map<String,Object>에서 "handlerType"을 키로 담은 Value)
     * @param fromMemberNo 보내는 사람
     * @param toMemberNo   받는 사람
     * @param order        해당 주문 객체
     * @return Optional<AlertHistoryDTO> 저장되지 않은 경우 빈 객체가 갈수도 있음
     */
    public Optional<AlertHistoryDTO> RiderAssignAlert(String alertType, int fromMemberNo, int toMemberNo,
            OrderHistoryDTO order) {

        AlertHistoryDTO dto = new AlertHistoryDTO();
        MemberDTO toMember = memberRepository.findByMemberNo(toMemberNo);
        MemberDTO fromMember = memberRepository.findByMemberNo(fromMemberNo);
        dto.setAlertForm(fromMember);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setAlertType(alertType);
        dto.setAlertTo(toMember);
        dto.setIsRead(0);
        dto.setStatus(0);
        dto.setOrder(order);

        try {
            AlertHistoryDTO saved = alertHistoryRepository.save(dto);
            return Optional.of(saved); // 성공 시 Optional 안에 값 담기
        } catch (Exception e) {
            return Optional.empty(); // 실패 시 빈 Optional
        }

    }
}
