package com.ex.admin.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ex.admin.model.data.RestrictDTO;
import com.ex.admin.model.repository.AdminMemberRepository;
import com.ex.admin.model.repository.RestrictRepository;
import com.ex.admin.model.service.AdminMemberService;
import com.ex.admin.model.service.AdminService;
import com.ex.admin.model.service.AdminShopService;
import com.ex.admin.model.service.RestrictService;
import com.ex.member.model.data.MemberDTO;
import com.ex.shop.model.data.ShopDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class AdminUserController {

    private final AdminService adminService;
    private final AdminMemberService adminMemberService;
    private final RestrictService restrictService;
    private final AdminMemberRepository adminMemberRepository;
    private final AdminShopService adminShopService;
    private final RestrictRepository restrictRepository;

    // 관리자페이지 입점신청
    @GetMapping("/registry")
    public String adminRegistry() {
        return "redirect:/admin/user/registry/list";
    }

    /**
     * 입점신청 목록
     * 요청 url http://localhost:8080/admin/user/registry/list
     */
    @GetMapping("/registry/list")
    public String registryList(@RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size,
            @RequestParam(defaultValue = "", name = "result") String result,
            Model model) {
        Page<ShopDTO> paging = adminShopService.getRegistryList(result, page, size);
        model.addAttribute("paging", paging);
        model.addAttribute("page", page);
        model.addAttribute("result", result);

        return "admin/user/registryList";
    }

    /** 입점신청 상세 */
    @GetMapping("/registry/detail/{shopNo}") // 조회
    public String registryDetail(@PathVariable("shopNo") int shopNo,
            @RequestParam(value = "result", defaultValue = "") String result,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        ShopDTO shop = this.adminService.getShop(shopNo);
        model.addAttribute("shop", shop);
        model.addAttribute("result", result);
        model.addAttribute("page", page);
        return "admin/user/registryDetail";
    }

    /** 입점신청 승인 */
    @PostMapping("/registry/approve/{shopNo}")
    @ResponseBody
    public String approveRegistry(@PathVariable("shopNo") int shopNo,
            @RequestParam(value = "result", required = false) String result,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        this.adminService.approveRegistry(shopNo);
        return "success";
    }

    /** 입점신청 거절 */
    @PostMapping("/registry/refuse/{shopNo}")
    @ResponseBody
    public String refuseRegistry(@PathVariable("shopNo") int shopNo,
            @RequestParam("shopRegReason") int shopRegReason,
            @RequestParam(value = "result", required = false) String result,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        this.adminService.refuseRegistry(shopNo, shopRegReason); // 서비스 호출
        return "success";
    }

    // 관리자페이지 폐점신청 목록
    /**
     * 폐점신청 목록
     * 요청 url http://localhost:8080/admin/user/closing/list
     */
    @GetMapping("/closing")
    public String adminClosing() {
        return "redirect:/admin/user/closing/list";
    }

    /**
     * 매장 폐점 현황
     */
    @GetMapping("/closing/list")
    public String closingList(@RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "closingAt"));
        Page<ShopDTO> paging = adminShopService.getClosedShopList(pageable);
        Map<Integer, Long> operatingDaysMap = new HashMap<>();
        for (ShopDTO shop : paging.getContent()) {
            long days = ChronoUnit.DAYS.between(
                    shop.getShopCreatedAt().toLocalDate(),
                    shop.getClosingAt().toLocalDate());
            operatingDaysMap.put(shop.getShopNo(), days);
        }

        model.addAttribute("paging", paging);
        model.addAttribute("operatingDaysMap", operatingDaysMap);
        return "admin/user/closingList";
    }

    /**
     * 회원관리
     * 요청 url http://localhost:8080/admin/user/member
     */
    @GetMapping("/member")
    public String adminMember() {
        return "redirect:/admin/user/member/list";
    }

    /**
     * 회원 목록 페이지
     * /admin/user/list?sortField=memberReg&sortDir=desc&searchType=id&keyword=test&page=0
     */
    @GetMapping("/member/list")
    public String memberList(
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size,
            @RequestParam(defaultValue = "memberReg", name = "sortField") String sortField,
            @RequestParam(defaultValue = "desc", name = "sortDir") String sortDir,
            @RequestParam(required = false, name = "roleValue") Integer roleValue,
            @RequestParam(required = false, name = "keyword") String keyword,
            Model model) {

        Page<MemberDTO> paging = adminMemberService.getMemberList(keyword, roleValue, sortField, sortDir, page, size);

        model.addAttribute("paging", paging);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("roleValue", roleValue);
        model.addAttribute("keyword", (keyword != null) ? keyword : "");

        return "admin/user/memberList";
    }

    /**
     * 특정 회원 상세보기 Fragment
     */
    @GetMapping("/member/detail/{memberNo}")
    public String memberDetail(@PathVariable("memberNo") int memberNo, Model model) {
        MemberDTO member = adminMemberService.getMember(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. ID=" + memberNo));
        RestrictDTO restrict = restrictRepository.findTopByMemberNo(memberNo);
        model.addAttribute("member", member);
        model.addAttribute("restrict", restrict);
        return "admin/user/memberDetail :: detailFragment"; // fragment만 반환
    }

    /**
     * 회원 제재
     * 
     */
    @PostMapping("/member/restrict/{memberNo}")
    @ResponseBody
    public String restrictMember(
            @PathVariable("memberNo") int memberNo,
            @RequestParam("reason") int reason,
            @RequestParam("day") int day,
            @RequestParam("type") String type) {
        restrictService.restrict(memberNo, reason, type, day);
        return "-1";
    }

    /**
     * 회원 제재 철회
     */
    @PostMapping("/member/release/{memberNo}")
    @ResponseBody
    public String releaseMember(@PathVariable("memberNo") int memberNo) {
        int restoredRole = restrictService.release(memberNo);
        return String.valueOf(restoredRole);
    }

    /**
     * 회원 제재 확인
     * 로그인 시 memberRole == -1 인 경우에만 실행
     */

    /**
     * 매장관리
     * 요청 url http://localhost:8080/admin/user/shop
     */
    @GetMapping("/shop")
    public String adminShop() {
        return "redirect:/admin/user/shop/list";
    }

    @GetMapping("/shop/list")
    public String shopList(@RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size,
            @RequestParam(defaultValue = "shopCreatedAt", name = "sortField") String sortField,
            @RequestParam(defaultValue = "desc", name = "sortDir") String sortDir,
            @RequestParam(required = false, name = "roleValue") Integer roleValue,
            @RequestParam(required = false, name = "keyword") String keyword,
            Model model) {

        Page<ShopDTO> paging = adminShopService.getShopList(keyword, roleValue, sortField, sortDir, page, size);

        model.addAttribute("paging", paging);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("roleValue", roleValue);
        model.addAttribute("keyword", (keyword != null) ? keyword : "");

        return "admin/user/shopList";
    }

    /**
     * 매장 상세 Fragment
     */
    @GetMapping("/shop/detail/{shopNo}")
    public String shopDetail(@PathVariable("shopNo") int shopNo, Model model) {
        ShopDTO shop = adminShopService.getShop(shopNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 매장이 존재하지 않습니다. ID=" + shopNo));
        RestrictDTO restrict = restrictRepository.findTopByMemberNoOrderByRestrictAtDesc(shop.getMemberNo());
        model.addAttribute("shop", shop);
        model.addAttribute("restrict", restrict);
        return "admin/user/shopDetail :: detailFragment";
    }

    /**
     * 매장 제재
     */
    @PostMapping("/shop/restrict/{shopNo}")
    @ResponseBody
    public String restrictShop(@PathVariable("shopNo") int shopNo,
            @RequestParam("reason") int reason,
            @RequestParam("type") String type,
            @RequestParam("day") int day) {
        int newStatus = restrictService.restrictShop(shopNo, reason, type, day);
        return String.valueOf(newStatus); // "1" (정지)
    }

    // 매장 제재 취소
    @PostMapping("/shop/release/{shopNo}")
    @ResponseBody
    public String releaseShop(@PathVariable("shopNo") int shopNo) {
        int restoredStatus = restrictService.releaseShop(shopNo);
        return String.valueOf(restoredStatus); // "0" (정상운영)
    }

    // 통계 페이지
    @GetMapping("/chart")
    public String userChart() {
        return "admin/user/userChart";
    }

}
