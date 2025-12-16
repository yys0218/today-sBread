package com.ex.center.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.ex.admin.model.service.AdminShopService;
import com.ex.center.model.data.RegistryForm;
import com.ex.center.model.service.RegistryService;
import com.ex.member.model.data.MemberDTO;
import com.ex.shop.model.data.ShopDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/center/registry")
public class RegistryController {

    private final RegistryService registryService;
    private final AdminShopService adminShopService;

    @GetMapping({ "", "/" })
    public String registryMain() {
        return "redirect:/center/registry/list";
    }

    /**
     * 입점신청 조회
     * url = localhost:8080/center/registry/list
     */
    @GetMapping("/list")
    public String listRegistry(@SessionAttribute("user") MemberDTO user,
            @RequestParam(defaultValue = "0", name = "page") int page,
            Model model) {
        int memberRole = user.getMemberRole();
        int memberNo = user.getMemberNo();
        Page<ShopDTO> paging = registryService.myRegistryList(memberNo, page);
        model.addAttribute("paging", paging);
        model.addAttribute("memberRole", memberRole);

        // 현재 입점 신청 중인지(shopRegResult가 null인지) 확인
        boolean isApplying = this.registryService.applyCheck(user);
        model.addAttribute("isApplying", isApplying);

        return "center/registryList";
    }

    /**
     * 입점신청 작성 AJAX
     * url = localhost:8080/center/registry/ajaxApplyCheck
     * return : 입점심사중 : false, 심사중아님 : true
     */
    @PostMapping("/ajaxApplyCheck")
    @ResponseBody
    public Map<String, Object> applyCheck(@SessionAttribute("user") MemberDTO user) {
        boolean result = this.registryService.applyCheck(user);

        Map<String, Object> response = new HashMap<>();
        response.put("result", result);

        return response; // => {"result": true} 혹은 {"result": false} 로 응답됨
    }

    /**
     * 입점신청 작성
     * url = localhost:8080/center/registry/apply
     * 입점신청 체크시 자동으로 이동
     */
    @GetMapping("/apply")
    public String applyRegistry(@SessionAttribute("user") MemberDTO user, RegistryForm registryForm, Model model) {
        int memberRole = user.getMemberRole();
        model.addAttribute("memberRole", user.getMemberRole());
        if (memberRole != 0) {
            return "redirect:/center/registry";
        }
        ShopDTO shop = this.registryService.getLastShop(user.getMemberNo());
        if (shop != null) {
            registryForm.setShopName(shop.getShopName());
            registryForm.setShopContact(shop.getShopContact());
            registryForm.setShopInfo(shop.getShopInfo());
            String full = shop.getShopAddress();
            String regex = "^(.*?(로|길)\\s*\\d+(-\\d+)?)";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(full);

            if (m.find()) {
                registryForm.setShopRoadAdd(m.group(1).trim());
                registryForm.setShopDetailAdd(full.substring(m.end()).trim());
            } else {
                registryForm.setShopRoadAdd(full); // 패턴 안 맞으면 전부 roadAdd
                registryForm.setShopDetailAdd("");
            }
            registryForm.setShopSido(shop.getShopSido());
            registryForm.setShopSigungu(shop.getShopSigungu());
            registryForm.setShopBname(shop.getShopBname());
            registryForm.setShopDayOff(shop.getShopDayOff());
            registryForm.setDeliveryFee(shop.getDeliveryFee());
            registryForm.setTinNo(shop.getTinNo());
            registryForm.setOpenTime(shop.getOpenTime());
            registryForm.setCloseTime(shop.getCloseTime());
            registryForm.setDeliveryFee(shop.getDeliveryFee());
            registryForm.setBusinessName(shop.getBusinessName());
            registryForm.setBusinessOpenAt(shop.getBusinessOpenAt());
            registryForm.setBusinessContact(shop.getBusinessContact());
            registryForm.setBusinessMail(shop.getBusinessMail());
            registryForm.setBusinessBank(shop.getBusinessBank());
            registryForm.setBusinessAccName(shop.getBusinessAccName());
            registryForm.setBusinessAccNum(shop.getBusinessAccNum());
        }
        return "center/registryForm";
    }

    @PostMapping("/apply")
    public String applyRegistry(@SessionAttribute("user") MemberDTO user,
            @Valid RegistryForm form, BindingResult br) {
        int memberRole = user.getMemberRole();
        if (br.hasErrors()) {
            return "center/registryForm";
        }
        int memberNo = user.getMemberNo();
        if (memberRole != 0)
            throw new IllegalStateException("구매자만 신청할 수 있습니다.");
        registryService.applyRegistry(memberNo, form);
        return "redirect:/center/registry";
    }
/**
     * 입점신청 수락하기
     * url = localhost:8080/center/registry/approve/{registryNo}
     */

    // @PostMapping("/approve/{shopNo}")
    // @ResponseBody
    // public String approveRegistry(@PathVariable("shopNo") int shopNo) {
    //     this.registryService.approveRegistry(shopNo);
    //     return "success";
    // }

        /**
     * 입점신청 취소
     * url = localhost:8080/center/registry/cancel/{registryNo}
     */
    @PostMapping("/cancel/{shopNo}")
    public String cencelRegistry(@SessionAttribute("user") MemberDTO user,
            @PathVariable("shopNo") int shopNo, Model model) {
        int memberNo = user.getMemberNo();
        if (memberNo != this.registryService.getShop(shopNo).getMemberNo()) {
            model.addAttribute("title", "오류");
            model.addAttribute("msg", "비정상적인 접근입니다.");
            model.addAttribute("icon", "error");
            model.addAttribute("loc", "/center");
            return "common/msg";
        }
        this.registryService.cancelRegistry(shopNo);
        model.addAttribute("title", "입점 신청 철회");
            model.addAttribute("msg", "철회되었습니다.");
            model.addAttribute("icon", "success");
            model.addAttribute("loc", "/center/registry");
            return "common/msg";
    }

    /** 가게명 중복 체크 */
    @GetMapping("/checkShopName")
    public ResponseEntity<Boolean> checkShopName(@RequestParam("shopName") String shopName) {
        boolean exists = adminShopService.shopNameCheck(shopName); 
        return ResponseEntity.ok(exists);
    }
    /** 가게연락처 중복 체크 */
    @GetMapping("/checkShopContact")
    public ResponseEntity<Boolean> checkShopContack(@RequestParam("shopContact") String shopContact){
        boolean exists = adminShopService.shopContactCheck(shopContact);
        return ResponseEntity.ok(exists);
    }
}
