package com.ex.admin.model.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.ex.DataNotFoundException;
import com.ex.admin.model.data.RestrictDTO;
import com.ex.admin.model.repository.AdminMemberRepository;
import com.ex.admin.model.repository.AdminShopRepository;
import com.ex.admin.model.repository.RestrictRepository;
import com.ex.member.model.data.MemberDTO;
import com.ex.shop.model.data.ShopDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestrictService {

    private final AdminMemberRepository adminMemberRepository;
    private final AdminShopRepository adminShopRepository;
    private final RestrictRepository restrictRepository;

    /**
     * 제제
     * @Param memberNo
     * @Param reason 제재 이유
     * @Param type 제재 유형('3일', '1주일', '1달', '영구정지')
     * @Param day 제재 '일'수(영정은 65536)
     *        제재 당일 23시 59분까지 제재
     */
    // 회원 제재
    @Transactional
    public int restrict(@Param("memberNo") int memberNo, @Param("reason") int reason, 
                        @Param("type") String type, @Param("day") int day) {
        // 제재 테이블 생성
        RestrictDTO restrict = new RestrictDTO();
        int role = this.adminMemberRepository.findMemberRoleByMemberNo(memberNo);
        restrict.setMemberNo(memberNo);
        restrict.setMemberRole(role);
        restrict.setRestrictReason(reason);
        restrict.setRestrictType(type);
        restrict.setRestrictPeriod(LocalDate.now().plusDays(day));
        restrict.setRestrictAt(LocalDateTime.now());
        this.restrictRepository.save(restrict);
        // MemberRole 변경
        Optional<MemberDTO> _member = this.adminMemberRepository.findById(memberNo);
        if (_member.isEmpty()) {throw new DataNotFoundException("회원 정보를 찾을 수 없습니다.");}
        MemberDTO member = _member.get();
        member.setMemberRole(-1); // 제재
        this.adminMemberRepository.save(member);
        return member.getMemberRole();
    }

    // 제재 취소
    @Transactional
    public int release(int memberNo){
        RestrictDTO restrict = this.restrictRepository.findTopByMemberNo(memberNo);
        int restrictNo = restrict.getRestrictNo();
        int role = restrict.getMemberRole();
        Optional<MemberDTO> _member = this.adminMemberRepository.findById(memberNo);
        if (_member.isEmpty()) {throw new DataNotFoundException("회원 정보를 찾을 수 없습니다.");}
        MemberDTO member = _member.get();
        member.setMemberRole(role); // 제재 전으로 되돌림
        this.adminMemberRepository.save(member);
        this.restrictRepository.deleteById(restrictNo);
        return role;
    }

    // 상점 제재
    @Transactional
    public int restrictShop(@Param("shopNo") int shopNo, @Param("reason") int reason, 
                        @Param("type") String type, @Param("day") int day) {
        // 제재 테이블 생성
        RestrictDTO restrict = new RestrictDTO();
        Optional<ShopDTO> _shop = this.adminShopRepository.findById(shopNo);
        if(_shop.isEmpty()){throw new DataNotFoundException("해당 매장을 찾을 수 없습니다.");}
        ShopDTO shop = _shop.get();
        int memberNo = shop.getMemberNo();
        int role = this.adminMemberRepository.findMemberRoleByMemberNo(memberNo);
        restrict.setMemberNo(memberNo);
        restrict.setMemberRole(role);
        restrict.setRestrictReason(reason);
        restrict.setRestrictType(type);
        restrict.setRestrictPeriod(LocalDate.now().plusDays(day));
        restrict.setRestrictAt(LocalDateTime.now());
        this.restrictRepository.save(restrict);
        // ShopStatus 변경
        shop.setShopStatus(1); // 1 : 정지
        this.adminShopRepository.save(shop);
        return shop.getShopStatus();
    }

    // 상점 제재 취소
    @Transactional
    public int releaseShop(int shopNo){
        int memberNo = this.adminShopRepository.findMemberNoByShopNo(shopNo);
        RestrictDTO restrict = this.restrictRepository.findTopByMemberNo(memberNo);
        int restrictNo = restrict.getRestrictNo();

        Optional<ShopDTO> _shop = this.adminShopRepository.findById(shopNo);
        if(_shop.isEmpty()){throw new DataNotFoundException("해당 매장을 찾을 수 없습니다.");}
        ShopDTO shop = _shop.get();

        shop.setShopStatus(0); // 정상 운영으로 되돌림
        this.adminShopRepository.save(shop);
        this.restrictRepository.deleteById(restrictNo);
        return shop.getShopStatus();
    }

    /**
     * 제재 정보 확인
     * @Param MemberNo
     * return RestrictDTO
     */
    public RestrictDTO getRestrict(int memberNo) {
        return this.restrictRepository.findTopByMemberNoOrderByRestrictNoDesc(memberNo);
    }

    /** 누적 제재 수 확인
     * @Param memberNo
     * return int
     */
    public int countRestrict(int memberNo){
        return this.restrictRepository.countByMemberNo(memberNo);
    }

    /**
     * 제재 확인(가장 최신것만)
     * memberRole = -1 인 경우에만 실행
     * @Param memberNo : Session 에서 memberNo 를 추출 삽입
     * result boolean result = true(제재 기간 내), false(제재 기간 지남)
     * true 시 로그인 시도 -> 거절
     */
    public boolean checkRestrict(int memberNo) {
        RestrictDTO restrict = this.restrictRepository.findTopByMemberNoOrderByRestrictNoDesc(memberNo);
        boolean result = LocalDate.now().isAfter(restrict.getRestrictPeriod());
        if (result == true) {
            Optional<MemberDTO> _member = this.adminMemberRepository.findById(memberNo);
            if (_member.isEmpty()) {throw new DataNotFoundException("회원 정보를 찾을 수 없습니다.");}
            MemberDTO member = _member.get();
            int role = restrict.getMemberRole();
            member.setMemberRole(role); // 제재 해제
            this.adminMemberRepository.save(member);
            this.restrictRepository.delete(restrict);
        }
        return result;
    }

    /**
     * 상점 제재 확인(가장 최신것만)
     * shopStatus == 1 인 경우에만 실행
     * @Param member : Session 에서 memberNo 를 추출 삽입
     * memberNo -> shopNo 추출
     * result boolean result = true(제재 기간 내), false(제재 기간 지남)
     * true 시 매장 페이지 시도 -> 거절
     */
    public boolean checkShopRestrict(int memberNo) {
        RestrictDTO restrict = this.restrictRepository.findTopByMemberNoOrderByRestrictNoDesc(memberNo);
        boolean result = !LocalDate.now().isAfter(restrict.getRestrictPeriod());
        if (result == false) {
            ShopDTO shop = this.adminShopRepository.findByMemberNo(memberNo);
            shop.setShopStatus(0); // 제재 해제
            
        }
        return result;
    }

    // // 공통 변환 메서드
    // private Map<String, Object> toMap(List<Object[]> result, Function<Object, String> labelMapper) {
    //     List<String> labels = new ArrayList<>();
    //     List<Long> data = new ArrayList<>();

    //     for (Object[] row : result) {
    //         labels.add(labelMapper.apply(row[0]));
    //         if (row[1] != null) {
    //             data.add(Long.parseLong(row[1].toString()));
    //         } else {
    //             data.add(0L);
    //         }
    //     }

    //     Map<String, Object> map = new HashMap<>();
    //     map.put("labels", labels);
    //     map.put("data", data);
    //     return map;
    // }

    // // 제재 사유별
    // public Map<String, Object> getRestrictReason() {
    //     return toMap(restrictRepository.findRestrictReason(),
    //             obj -> {
    //                 if (obj == null) return "알수없음";
    //                 int reason = ((Number) obj).intValue();
    //                 return switch (reason) {
    //                     case 0 -> "운영방침 위반";
    //                     case 1 -> "부적절한 이용행위";
    //                     case 2 -> "결제관련 문제";
    //                     case 3 -> "법적 문제";
    //                     case 4 -> "판매자 피해 누적";
    //                     case 5 -> "위생 및 품질 문제";
    //                     case 6 -> "배송·운영 불성실";
    //                     case 7 -> "허위·과장 광고";
    //                     case 8 -> "정산·계약 위반";
    //                     case 9 -> "소비자 피해 누적";
    //                     default -> "알수없음";
    //                 };
    //             });
    // }

    // // 제재 유형별
    // public Map<String, Object> getRestrictType() {
    //     return toMap(restrictRepository.findRestrictType(),
    //             obj -> {
    //                 String type = String.valueOf(obj);
    //                 return switch (type) {
    //                     case "3일" -> "3일 정지";
    //                     case "1주일" -> "1주일 정지";
    //                     case "1달" -> "1달 정지";
    //                     case "영구정지" -> "영구정지";
    //                     default -> "알수없음";
    //                 };
    //             });
    // }

    // // 월별 제재 건수
    // public Map<String, Object> getRestrictByMonth() {
    //     return toMap(restrictRepository.findRestrictByMonth(),
    //             obj -> String.valueOf(obj)); // 그대로 "2025-09"
    // }
    
}
