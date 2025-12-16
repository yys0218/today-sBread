package com.ex.center.model.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClosingService {
    
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;
    
    @Transactional
    public void closingShop(int shopNo, int type){
        Optional<ShopDTO> _shop = this.shopRepository.findById(shopNo);
        if (_shop.isEmpty()) {
            throw new UsernameNotFoundException("해당하는 상점을 찾을 수 없습니다.");
        }
        ShopDTO shop = _shop.get();
        int memberNo = shop.getMemberNo();
        MemberDTO member = this.memberRepository.findByMemberNo(memberNo);
        shop.setShopStatus(2);
        shop.setClosingAt(LocalDateTime.now());
        member.setMemberRole(0);
    }
}
