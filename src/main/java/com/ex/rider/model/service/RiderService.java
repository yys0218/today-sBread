package com.ex.rider.model.service;

import java.beans.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.ex.common.MailSend;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.data.MemberInsertForm;
import com.ex.member.model.repository.MemberRepository;
import com.ex.member.model.service.MemberService;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.data.OrderTimeDTO;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.service.ShoppingCartService;
import com.ex.rider.model.data.CommunityEmotionsDTO;
import com.ex.rider.model.data.DeliveryFeeDTO;
import com.ex.rider.model.data.RiderCommunityDTO;
import com.ex.rider.model.data.RiderInsertForm;
import com.ex.rider.model.repository.CommunityEmotionsRepository;
import com.ex.rider.model.repository.CommunityRepository;
import com.ex.rider.model.repository.DeliveryFeeRepository;
import com.ex.rider.model.repository.RiderRepository;
import com.ex.shop.model.repository.ShopOrderHistoryRepository;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final MemberService memberService;

    private final MemberRepository memberRepository;

    private final ShopOrderHistoryRepository shopOrderHistoryRepository;

    private final ShoppingCartService shoppingCartService;

    private final RiderRepository riderRepository;

    private final CommunityRepository communityRepository;

    private final CommunityEmotionsRepository communityEmotionsRepository;

    private final OrderHistoryRepository orderHistoryRepository;

    private final DeliveryFeeRepository deliveryFeeRepository;

    private final KakaoLocalService kakaoLocalService;

    private final PasswordEncoder passwordEncoder;

    private final UserDetailsService userDetailsService;

    private final MailSend mailSend;

    // 아이디 중복검사
    public boolean ajaxDuplicationId(String memberId) {
        boolean result = false;
        Optional _memberId = this.riderRepository.findByMemberId(memberId);
        // 조회 결과가 없을 경우 사용가능 아이디 이므로 result에 true
        if (!_memberId.isPresent()) {
            result = true;
        }
        return result;
    }

    // 전화번호 중복검사
    public boolean ajaxDuplicationPhone(String memberPhone) {
        boolean result = false;
        Optional _memberPhone = this.riderRepository.ajaxFindByMemberPhone(memberPhone);
        // 저회 결과가 없을 경우
        if (_memberPhone.isEmpty()) {
            result = true;
        }
        return result;
    }

    public boolean ajaxDuplicationEmail(String memberEmail) {
        boolean result = false;
        Optional<String> _memberEmail = this.riderRepository.ajaxFindByMemberEmail(memberEmail);
        if (!_memberEmail.isPresent()) {
            result = true;
        }
        return result;

    }

    // 회원가입
    public void insertMember(RiderInsertForm riderInsertForm) {

        MemberDTO dto = new MemberDTO();
        dto.setMemberId(riderInsertForm.getMemberId());
        dto.setMemberPw(passwordEncoder.encode(riderInsertForm.getMemberPw())); // 비밀번호 암호화
        dto.setMemberName(riderInsertForm.getMemberName());
        dto.setMemberNick(riderInsertForm.getMemberId());
        dto.setMemberEmail(riderInsertForm.getEmailLocal() + "@" + riderInsertForm.getEmailDomain());
        dto.setMemberBirth(riderInsertForm.getMemberBirth());
        dto.setMemberAddress(riderInsertForm.getRoadAddress() + riderInsertForm.getDetailAddress());
        dto.setMemberPhone(riderInsertForm.getMemberPhone());
        dto.setMemberRole(4); // 라이더 권한 4 : RIDER
        dto.setMemberReg(LocalDateTime.now());
        try {
            Map<String, Double> map = this.kakaoLocalService.getCoordinates(dto.getMemberAddress());
            dto.setLatitude(map.get("latitude"));
            dto.setLongitude(map.get("longitude"));
        } catch (Exception e) {
            // TODO: handle exception
        }
        riderRepository.save(dto);
    }

    // 회원가입
    public void dummyRider() {

        MemberDTO dto = new MemberDTO();
        dto.setMemberId("mementous0");
        dto.setMemberPw(passwordEncoder.encode("1234")); // 비밀번호 암호화
        dto.setMemberName("안병주");
        dto.setMemberNick("mementous0");
        dto.setMemberEmail("mementous0@gmail.com");
        dto.setMemberBirth(LocalDate.parse("1997-09-15"));
        dto.setMemberAddress("서울특별시 관악구 남부순환로 1820 6층 글로벌아이티 6B");
        dto.setMemberPhone("01056698920");
        dto.setMemberRole(4); // 라이더 권한 4 : RIDER
        dto.setMemberReg(LocalDateTime.now());
        Map<String, Double> map = this.kakaoLocalService.getCoordinates(dto.getMemberAddress());
        dto.setLatitude(map.get("latitude"));
        dto.setLongitude(map.get("longitude"));

        riderRepository.save(dto);
    }

    // 아이디 찾기
    public String ajaxFindRiderId(String memberName, String memberEmail) {
        Optional<String> _riderId = this.riderRepository.ajaxFindMemberId(memberName, memberEmail);
        String riderId = "";
        // 조회된 _memberId가 있을 경우
        if (_riderId.isPresent()) {
            riderId = _riderId.get();
        } else {
            riderId = null;
        }
        return riderId;
    }

    //
    /**
     * 비밀번호 찾기
     * 
     * @param memberId    회원 아이디
     * @param memberEmail 회원 이메일
     * @return
     *         <ul>
     *         <li>- memberNotFound : 회원 조회 실패 (아이디,이메일 불일치)</li>
     *         <li>- mailSendError : 이메일 전송 실패 (이메일 전송 오류)</li>
     *         <li>- success : 임시비밀번호 변경 완료 및 이메일 전송 완료</li>
     *         </ul>
     */
    @Transactional
    public String ajaxFindRiderPw(String memberId, String memberEmail) {
        Optional<MemberDTO> _member = this.riderRepository.findByMemberIdAndMemberEmailAndMemberRole(memberId,
                memberEmail, 4);

        if (_member.isEmpty()) {
            return "memberNotFound";
        } else {
            MemberDTO member = _member.get();
            String password = this.memberService.generateTempPassword();

            member.setMemberPw(passwordEncoder.encode(password));
            member.setIsTemporary(1);

            this.memberRepository.save(member);

            try {
                mailSend.sendPwMail(memberEmail, password);
            } catch (MessagingException e) {

                e.printStackTrace();
                // 트랜잭션 롤백만 수행 (예외는 던지지 않음)
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return "mailSendError";
            }
            return "success";
        }
    }

    @Transactional
    public void changePassword(MemberDTO member, String newPassword) {
        member.setMemberPw(passwordEncoder.encode(newPassword));
        member.setIsTemporary(0);
        this.memberRepository.save(member);

        // 시큐리티 세션 갱신
        UserDetails updatedUser = userDetailsService.loadUserByUsername(member.getMemberId());
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser,
                updatedUser.getPassword(),
                updatedUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    // 커뮤니티 글 작성하기
    public void ajaxCommunityInsert(MemberDTO member, String communityContent) {
        RiderCommunityDTO dto = new RiderCommunityDTO();
        dto.setCommunityContent(communityContent);
        dto.setCreateAt(LocalDateTime.now());
        dto.setMember(member);

        this.communityRepository.save(dto);
    }

    // 게시글 10개 가져오기 (초기화면)
    public List<RiderCommunityDTO> getList() {
        return communityRepository.findTop10ByOrderByCreateAtDesc();
    }

    public RiderCommunityDTO findBycommunityNo(Long communityNo) {
        Optional<RiderCommunityDTO> _dto = this.communityRepository.findByCommunityNo(communityNo);
        if (_dto.isEmpty()) {
            return null;
        }
        return _dto.get();

    }

    // 라이더 이모지 클릭시 삽입 / 변환
    public void emotions(MemberDTO member, RiderCommunityDTO dto, int emotionsType) {
        CommunityEmotionsDTO existing = null;
        // 기존 감정 찾기 (for문 사용)
        for (CommunityEmotionsDTO e : dto.getEmotions()) {
            if (e.getMember().getMemberNo() == member.getMemberNo()) {
                existing = e;
                break;
            }
        }
        if (existing != null) {
            // 이미 반응이 있는 경우
            if (existing.getEmotionsType() == emotionsType) {
                // 같은 반응 다시 누르면 삭제
                dto.getEmotions().remove(existing);
                this.communityEmotionsRepository.delete(existing);
            } else {
                // 다른 반응으로 변경
                existing.setEmotionsType(emotionsType);
                existing.setCreatedAt(LocalDateTime.now());
                communityEmotionsRepository.save(existing);
            }
        } else {
            // 반응이 없으면 새로 추가
            CommunityEmotionsDTO newEmotion = new CommunityEmotionsDTO();
            newEmotion.setCommunity(dto);
            newEmotion.setMember(member);
            newEmotion.setEmotionsType(emotionsType);
            newEmotion.setCreatedAt(LocalDateTime.now());

            dto.getEmotions().add(newEmotion); // 양방향 연관관계 동기화
            communityEmotionsRepository.save(newEmotion);
        }
    }

    /**
     * 라이더 주문 내역 가져오기
     * 
     * @param type   (all:전체목록 , stay:배송대기목록 , accept:배송수락(픽업전) , delivery:배송중(픽업후)
     *               , complete:배송완료)
     * @param member (member객체)
     * 
     * @return List<OrderHistoryDTO> (OrderHistory객체를 10개 담은 List객체 )
     * 
     */
    public List<OrderHistoryDTO> getOrderHistory(String type, MemberDTO member) {
        List<OrderHistoryDTO> list = new ArrayList<>();
        if (type.equals("all")) {
            list = this.orderHistoryRepository.findTop10ByStatusGreaterThanEqualOrderByOrderNoDesc(2);
        } else if (type.equals("stay")) {
            list = this.orderHistoryRepository.findTop10ByStatusOrderByOrderNoDesc(2);
        } else if (type.equals("accept")) {
            list = this.orderHistoryRepository.findTop10ByStatusAndRiderOrderByOrderNoDesc(3, member);
        } else if (type.equals("delivery")) {
            list = this.orderHistoryRepository.findTop10ByStatusAndRiderOrderByOrderNoDesc(4, member);
        } else if (type.equals("complete")) {
            list = this.orderHistoryRepository.findTop10ByStatusAndRiderOrderByOrderNoDesc(5, member);
        }
        return list;
    }

    // 라이더 배송 요청 수락
    public void riderAccpetOrder(OrderHistoryDTO order) {
        // TODO Auto-generated method stub
        // 배송 시간 관련 테이블에 라이더가 요청을 수락한 현재시간 insert
        order.getOrderTime().setAssignedAt(LocalDateTime.now());
        order.setStatus(3);
        this.shopOrderHistoryRepository.save(order);
    }

    public Map<String, Integer> riderChart(MemberDTO member) {
        Map<String, Integer> result = new HashMap<>();

        LocalDateTime today = LocalDateTime.now();
        YearMonth currentMonth = YearMonth.from(today);

        Map<String, YearMonth> months = Map.of(
                "thisMonth", currentMonth,
                "lastMonth", currentMonth.minusMonths(1),
                "twoMonthsAgo", currentMonth.minusMonths(2));

        for (Map.Entry<String, YearMonth> entry : months.entrySet()) {
            String key = entry.getKey();
            YearMonth ym = entry.getValue();

            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999);

            List<DeliveryFeeDTO> list = deliveryFeeRepository
                    .findByMemberAndFeeTypeAndCreatedAtBetween(member, start, end);

            int totalOrderCompletes = list.size();
            int totalFee = list.stream()
                    .mapToInt(DeliveryFeeDTO::getFeeAmount)
                    .sum();

            result.put(key + "OrderCount", totalOrderCompletes);
            result.put(key + "TotalFee", totalFee);
        }

        return result;
    }

    public Map<String, Integer> ajaxUpdateChart(MemberDTO member, int year, int month) {
        Map<String, Integer> result = new HashMap<>();

        YearMonth currentMonth = YearMonth.of(year, month);

        Map<String, YearMonth> months = Map.of(
                "thisMonth", currentMonth,
                "lastMonth", currentMonth.minusMonths(1),
                "twoMonthsAgo", currentMonth.minusMonths(2));

        for (Map.Entry<String, YearMonth> entry : months.entrySet()) {
            String key = entry.getKey();
            YearMonth ym = entry.getValue();

            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999);

            List<DeliveryFeeDTO> list = deliveryFeeRepository
                    .findByMemberAndFeeTypeAndCreatedAtBetween(member, start, end);

            int totalOrderCompletes = list.size();
            int totalFee = list.stream()
                    .mapToInt(DeliveryFeeDTO::getFeeAmount)
                    .sum();

            result.put(key + "OrderCount", totalOrderCompletes);
            result.put(key + "TotalFee", totalFee);
        }

        return result;
    }

    public void ajaxUpdateMemberInfo(Map<String, String> params, MemberDTO member) {
        // TODO Auto-generated method stub
        Set<String> keys = params.keySet();
        // item : address , phone , name , email , password
        for (String item : keys) {
            String value = params.get(item);
            System.out.println("item : " + item);
            System.out.println("value : " + value);
            switch (item) {
                case "address":
                    member.setMemberAddress(value);
                    break;
                case "phone":
                    member.setMemberPhone(value);
                    break;
                case "name":
                    member.setMemberName(value);
                    break;
                case "email":
                    member.setMemberEmail(value);
                    break;
                case "password":
                    member.setMemberPw(passwordEncoder.encode(value));
                    break;
            }
        }
        this.memberRepository.save(member);

        // 3. 시큐리티 세션 갱신
        UserDetails updatedUser = userDetailsService.loadUserByUsername(member.getMemberId());
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser,
                updatedUser.getPassword(),
                updatedUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    public List<DeliveryFeeDTO> findTop10ByMemberOrderByCreatedAtDesc(MemberDTO member, LocalDateTime startTarget,
            LocalDateTime endTarget) {
        List<DeliveryFeeDTO> request = this.deliveryFeeRepository
                // .findTop10ByMemberAndCreatedAtBetweenOrderByCreatedAtDesc(member,
                // startTarget, endTarget);
                .findByMemberAndCreatedAtBetweenOrderByCreatedAtDesc(member, startTarget, endTarget);
        return request;
    }

}
