package com.ex.rider.model.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ex.member.model.data.MemberDTO;
import com.ex.rider.model.data.RiderSecurityDetail;
import com.ex.rider.model.repository.DeliveryFeeRepository;
import com.ex.rider.model.repository.RiderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiderDetailsService implements UserDetailsService {

    private final RiderRepository riderRepository;

    private final DeliveryFeeRepository deliveryFeeRepository;

    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        // 라이더 아이디 있는지 조회
        // 없을경우 없는 아이디라고 알려줌
        MemberDTO rider = riderRepository.riderLogin(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("없는 아이디입니다."));

        // ✅ 최신 잔액 조회
        Integer latestBalance = deliveryFeeRepository.findLatestFeeBalanceByMemberNo(rider.getMemberNo());
        rider.setBalance(latestBalance != null ? latestBalance : 0);

        // UserDetails 를 사용해서 새로운 클래스를 만든 뒤 해당 클래스에를 시큐리티인증에 사용
        return new RiderSecurityDetail(rider);

        // // 시큐리티가 이해할 수 있는 User 객체로 변환
        // return org.springframework.security.core.userdetails.User
        // .withUsername(rider.getMemberId()) // 아이디
        // .password(rider.getMemberPw()) // 암호화된 비번
        // // .roles(rider.getRole()) // 권한
        // .build();
    }// TODO Auto-generated method stub
}
