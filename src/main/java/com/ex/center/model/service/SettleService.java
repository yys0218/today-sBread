package com.ex.center.model.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ex.DataNotFoundException;
import com.ex.admin.model.data.SettleConfigHistory;
import com.ex.admin.model.data.SettleDTO;
import com.ex.admin.model.repository.AdminSettleConfigHistoryRepository;
import com.ex.admin.model.repository.AdminSettleRepository;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.rider.model.data.DeliveryFeeDTO;
import com.ex.rider.model.repository.DeliveryFeeRepository;
import com.ex.shop.model.data.SalesHistoryDTO;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.SalesHistoryRepository;
import com.ex.shop.model.repository.ShopRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettleService {

    private final MemberRepository memberRepository;
    private final SalesHistoryRepository salesHistoryRepository;
    private final ShopRepository shopRepository;
    private final DeliveryFeeRepository deliveryFeeRepository;
    private final AdminSettleConfigHistoryRepository adminSettleConfigHistoryRepository;
    private final AdminSettleRepository adminSettleRepository;

    /**
     * 정산 신청 시 작동하는 메서드
     * 프로세스
     * 1. memberNo 로 멤버 특정 -> memberRole 추출
     * 2-1. 상점이면 상점DTO를 memberNo로 특정 -> shopNo 추출 -> shopNo로 S.H 추출 -> 3번
     * 2-2. 라이더면 배달비DTO 를 memberNo 로 특정 -> 3번
     * 3. 최후 잔액을 추출, newDTO 에 정산(환전), 금액(최후잔액), 잔액(0), .now() 삽입
     */
    @Transactional
    public String apply(int memberNo) {
        // 수수료 조회
        SettleConfigHistory config = this.adminSettleConfigHistoryRepository.findTopByOrderByUpdatedAtDesc();
        // memberRole 조회
        Optional<MemberDTO> _member = this.memberRepository.findById(memberNo);
        if (_member.isEmpty()) {
            throw new DataNotFoundException("유저를 찾을 수 없습니다.");
        }
        MemberDTO member = _member.get();
        int memberRole = member.getMemberRole();
        if (memberRole == 1) {
            // 잔액 조회
            ShopDTO shop = this.shopRepository.findTopByMemberNoAndShopRegResult(memberNo, "Y");
            Optional<SalesHistoryDTO> _sh = this.salesHistoryRepository
                    .findTopByShopNoOrderByCreatedAtDesc(shop.getShopNo());
            if (_sh.isEmpty()) {

                return "error";
            } // 오류
            SalesHistoryDTO sh = _sh.get();
            Integer balance = sh.getSalesBalance();
            int lastBal = (balance != null) ? balance : 0;
            if (lastBal == 0) {
                return "zero";
            } // 정산할 금액이 업승ㅁ

            // 정산금액
            int settleAmount = lastBal * (100 - config.getShopRatio()) / 100;

            // 정산 후 잔액 변경
            int shopNo = shop.getShopNo();
            this.salesHistoryRepository.newSh(shopNo, settleAmount);

            // 정산 내역 저장
            SettleDTO newSt = new SettleDTO();
            newSt.setSettleType(memberRole);
            newSt.setSettleRef(memberNo);
            newSt.setSettleName(shop.getShopName());
            newSt.setSettleAmt(settleAmount);
            newSt.setSettleCharge(lastBal - settleAmount);
            newSt.setSettleAt(LocalDateTime.now());
            adminSettleRepository.save(newSt);
            return "success"; // 성공

        }
        if (memberRole == 4) {
            // 잔액 조회
            Optional<DeliveryFeeDTO> _df = this.deliveryFeeRepository.findTop1ByMemberOrderByFeeNoDesc(member);
            if (_df.isEmpty()) {return "error";} // 오류
            DeliveryFeeDTO df = _df.get();
            Integer balance = df.getFeeBalance();
            int lastBal = (balance != null) ? balance : 0;
            if (lastBal == 0) {return "zero";} // 정산금액 무

            // 수수료 계산(홈페이지 수익)
            int settleAmount = lastBal * (100 - config.getShopRatio()) / 100;

            // 정산 후 잔액 변경
            DeliveryFeeDTO newDf = new DeliveryFeeDTO();
            newDf.setMember(member);
            newDf.setFeeType(2);
            newDf.setFeeAmount(lastBal);
            newDf.setFeeBalance(0);
            newDf.setCreatedAt(LocalDateTime.now());
            this.deliveryFeeRepository.save(newDf);

            // 정산 내역 저장
            SettleDTO newSt = new SettleDTO();
            newSt.setSettleType(memberRole);
            newSt.setSettleRef(memberNo);
            newSt.setSettleName(member.getMemberNick());
            newSt.setSettleAmt(settleAmount);
            newSt.setSettleCharge(lastBal - settleAmount);
            newSt.setSettleAt(LocalDateTime.now());
            adminSettleRepository.save(newSt);
            return "success"; // 성공
        }
        return "fail"; // 실패
    }
}
