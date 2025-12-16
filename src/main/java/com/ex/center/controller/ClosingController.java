package com.ex.center.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ex.DataNotFoundException;
import com.ex.center.model.service.ClosingService;
import com.ex.member.model.data.MemberDTO;
import com.ex.shop.model.data.SalesHistoryDTO;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.SalesHistoryRepository;

import com.ex.shop.model.repository.ShopRepository;
import com.ex.shop.model.service.ShopService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center/closing")
public class ClosingController {

    private final SalesHistoryRepository salesHistoryRepository;
    private final ShopService shopService;
    private final ShopRepository shopRepository;
    private final ClosingService closingService;

    @GetMapping("")
    public String closing() {
        return "redirect:/center/closing/list";
    }

    @GetMapping("/list")
    public String closingList(@SessionAttribute("user") MemberDTO user, Model model) {

        model.addAttribute("memberRole", user.getMemberRole());
        int memberNo = user.getMemberNo();
        ShopDTO shop = this.shopService.getMyShopByMemberNo(memberNo);
        model.addAttribute("shop", shop);
        return "center/closingList";
    }

    @PostMapping("/closing/{shopNo}")
    public String closingShop(@PathVariable("shopNo") int shopNo, @RequestParam("closingReason") Integer closingReason,
            Model model) {
        ShopDTO shop = this.shopRepository.findByShopNo(shopNo);
        int lastBal;
        Optional<SalesHistoryDTO> _sh = this.salesHistoryRepository
                .findTopByShopNoOrderBySalesNoDesc(shop.getShopNo());
        if (_sh.isEmpty()) {
            lastBal = 0;
        }else{
        SalesHistoryDTO sh = _sh.get();
        lastBal = sh.getSalesBalance();}
        if (lastBal != 0) {
            model.addAttribute("title", "경고");
            model.addAttribute("msg", "정산할 금액이 남아있습니다.");
            model.addAttribute("icon", "warning");
            model.addAttribute("loc", "/center/closing/list");
            return "common/msg"; // 메인으로 이동하기 전 알림 띄우기

        }
        this.closingService.closingShop(shopNo, closingReason);
        model.addAttribute("title", "폐점 신청 완료");
        model.addAttribute("msg", "폐점하였습니다. 그동안 수고하셨습니다.");
        model.addAttribute("icon", "success");
        model.addAttribute("loc", "/");
        return "common/msg"; // 메인으로 이동하기 전 알림 띄우기
    }
}
