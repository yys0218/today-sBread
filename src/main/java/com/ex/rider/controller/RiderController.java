package com.ex.rider.controller;

import java.io.File;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
// Controller 임포트
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
// RequestMapping 임포트
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ex.common.MailSend;
import com.ex.common.StringUtil;
import com.ex.member.controller.AddressBookController;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.data.MemberInsertForm;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.service.ShoppingCartService;
import com.ex.rider.model.data.CommunityEmotionsDTO;
import com.ex.rider.model.data.DeliveryFeeDTO;
import com.ex.rider.model.data.RiderCommunityDTO;
import com.ex.rider.model.data.RiderInsertForm;
import com.ex.rider.model.data.RiderSecurityDetail;
import com.ex.rider.model.service.KakaoLocalService;
import com.ex.rider.model.service.KakaoMobilityService;
import com.ex.rider.model.service.RiderService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// 해당 클래스가 Spring MVC의 컨트롤러 역할을 하는 클래스임을 Spring에게 알려줌
@Controller
// http://localhost:8080/rider로 시작하는 모든 요청 URL을 이 컨트롤러에서 처리하도록 매핑
@RequestMapping("/rider")
@RequiredArgsConstructor
public class RiderController {

    private final AddressBookController addressBookController;

    private final StringUtil stringUtil;

    private final RiderService riderService;

    private final KakaoLocalService kakaoLocalService;

    private final KakaoMobilityService kakaoMobilityService;

    private final ShoppingCartService shoppingCartService;

    private final UserDetailsService userDetailsService;

    private final MailSend mailSend;

    // 페이지 접속 시 메인페이지(로그인 페이지로 이동)
    @GetMapping("")
    public String main(Principal principal) {
        if (principal == null) {
            return "rider/rider-login";
        } else {
            return "redirect:/rider/main";
        }
    }

    // 로그인 페이지 이동
    @GetMapping("/login")
    public String login() {
        return "rider/rider-login";
    }

    @GetMapping("/layout1")
    public String layout1() {
        return "rider/rider-layout1";
    }

    // @AuthenticationPrincipal RiderSecurityDetail principal

    // 라이더의 위치 정보를 세션에 저장
    // @PostMapping("/location/update")
    // public void ajaxLocationUpdate(@RequestParam("latitude") Double latitude,
    // @RequestParam("longitude") Double longitude, HttpSession session) {
    // Map<String, Double> location = new HashMap<String, Double>();
    // if (latitude != null && longitude != null) {
    // location.put("latitude", latitude);
    // location.put("longitude", longitude);
    // session.setAttribute("location", location);
    // } else {
    // session.setAttribute("location", null);
    // }
    // }
    @PostMapping("/location/update")
    @ResponseBody
    public void ajaxLocationUpdate(@RequestBody Map<String, Double> location, HttpSession session) {
        session.removeAttribute("location");
        System.out.println("latitude : " + location.get("latitude"));
        System.out.println("longitude : " + location.get("longitude"));
        Double latitude = location.get("latitude");
        Double longitude = location.get("longitude");

        if (latitude != null && longitude != null) {
            session.setAttribute("location", location);
        } else {
            session.setAttribute("location", null);
        }
    }

    // 로그인 후 처음 보는 페이지 [전체목록]
    @GetMapping("/main")
    // 로그인 여부 확인 로그인하지 않았을시 로그인페이지로 이동
    @PreAuthorize("isAuthenticated()")
    public String allView(Model model, @AuthenticationPrincipal RiderSecurityDetail principal) {

        List<OrderHistoryDTO> list = this.riderService.getOrderHistory("all", principal.getMember());

        model.addAttribute("list", list);

        return "rider/rider-all";
    }

    // 전체 목록 무한 스크롤 구현
    @PostMapping("/moreScroll")
    @ResponseBody
    public List<OrderHistoryDTO> moreScroll(@RequestParam("orderNo") int orderNo,
            @RequestParam("orderType") String orderType, @RequestParam("status") int status,
            @AuthenticationPrincipal RiderSecurityDetail principal) {
        // TODO: process POST request

        List<OrderHistoryDTO> list = this.shoppingCartService.getMoreScroll(orderNo, orderType, status);
        for (OrderHistoryDTO dto : list) {
            if (status != 2) {
                if (dto.getRider().getMemberNo() != principal.getMember().getMemberNo()) {
                    dto.setOrderAddress(stringUtil.maskAddress(dto.getOrderAddress()));
                    dto.setOrderPhone(stringUtil.maskPhone(dto.getOrderPhone()));
                } else {
                    dto.setOrderPhone(stringUtil.formatPhone(dto.getOrderPhone()));
                }
            }

        }
        return list;
    }

    // OrderNo로 OrderHistory 조회
    @PostMapping("/ajaxOrderDetail")
    @ResponseBody
    public OrderHistoryDTO ajaxOrderDetail(@RequestParam("orderNo") int orderNo,
            @AuthenticationPrincipal RiderSecurityDetail principal) {
        OrderHistoryDTO dto = this.shoppingCartService.ajaxOrderDetail(orderNo);
        MemberDTO rider = dto.getRider();
        MemberDTO member = principal.getMember();
        int status = dto.getStatus();
        System.out.println(rider);
        System.out.println(member);
        System.out.println(status);
        System.out.println(dto);
        if (status > 2) {
            if (rider.getMemberNo() != member.getMemberNo()) {
                dto.setOrderAddress(stringUtil.maskAddress(dto.getOrderAddress()));
                dto.setOrderPhone(stringUtil.maskPhone(dto.getOrderPhone()));
            } else {
                dto.setOrderPhone(stringUtil.formatPhone(dto.getOrderPhone()));
            }
        } else {
            dto.setOrderPhone(stringUtil.formatPhone(dto.getOrderPhone()));
        }

        return dto;
    }

    // 라이더 배송 수락
    // 조건 : 1. 해당 주문이 있어야됌
    // 조건 : 2. 해당 주문에 라이더가 없어야 하며 , 주문 상태가 점주가 주문수락 (2) 일때
    @PostMapping("/ajaxOrderAccept")
    @ResponseBody
    public String ajaxOrderAccept(@RequestParam("orderNo") int orderNo,
            @RequestParam("durationMin") int durationMin,
            @AuthenticationPrincipal RiderSecurityDetail principal) {
        // TODO: process POST request
        OrderHistoryDTO order = this.shoppingCartService.ajaxOrderAccept(orderNo);

        if (order == null) { // 해당 주문이 없을 때

            return "invalid access";
        } else { // 해당 주문이 있을 때
            if (order.getRider() != null && order.getStatus() > 2) {
                // rider 가 null이 아닐 때 즉 이미 라이더가 정해졌을때
                // 주문의 상태가 2(점주가 주문 수락) 한 것이 아닐 때
                return "already in use";
            } else { // rider가 정해지지 않은 배달일 경우

                System.out.println("durationMin : " + durationMin);
                order.setRider(principal.getMember());
                order.getOrderTime().setEstPickupAt(LocalDateTime.now().plusMinutes(durationMin));
                this.riderService.riderAccpetOrder(order);
                return "success";
            }
        }

    }

    // 라이더 상품 픽업
    @PostMapping("/ajaxPickupAccept")
    @ResponseBody
    public String ajaxPickupAccept(@RequestParam("orderNo") int orderNo, @RequestParam("durationMin") int durationMin) {
        // TODO: process POST request
        String response = "fail";
        response = this.shoppingCartService.ajaxPickupAccept(orderNo, durationMin);
        return response;
    }

    // 라이더 배송 완료
    @PostMapping("/ajaxCompleteAccept")
    @ResponseBody
    public String ajaxCompleteAccept(
            @RequestParam("orderNo") int orderNo,
            @RequestParam("file") MultipartFile file) {

        System.out.println("orderNo = " + orderNo);
        System.out.println("file name = " + file.getOriginalFilename());
        // 파일 이름 중복 방지
        String newName = UUID.randomUUID().toString().replace("-", "") + file.getOriginalFilename(); // UUID로 생성한 "-"뺀
                                                                                                     // 난수 + 원래 파일명

        // 파일 업로드 경로 지정;
        // //뒤에 파일명이 와야하므로 \\까지 넣어주기
        String response = "fail";
        String filePath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\upload\\rider\\" + newName;
        File files = new File(filePath); // 지정된 경로 + 새 파일명의 파일객체 생성
        if (file.getContentType().split("/")[0].equals("image")) { // 파일 유형이 image인 것만
            try {
                file.transferTo(files); // 멀티파트파일 객체에서 file로 파일 복사(업로드)
                response = this.shoppingCartService.ajaxCompleteAccept(orderNo, filePath);
            } catch (Exception e) { // 업로드 실패
                e.printStackTrace();
                return "fileNotFound"; // 500에러 반환
            }
        } // 업로드 종료

        return response;
    }

    // 대기 목록 리스트 페이지 [대기]
    @GetMapping("/stay")
    @PreAuthorize("isAuthenticated()")
    public String stayView(Model model, @AuthenticationPrincipal RiderSecurityDetail principal) {

        List<OrderHistoryDTO> list = this.riderService.getOrderHistory("stay", principal.getMember());

        model.addAttribute("list", list);

        return "rider/rider-stay";
    }

    @GetMapping("/accept")
    @PreAuthorize("isAuthenticated()")
    public String acceptView(Model model, @AuthenticationPrincipal RiderSecurityDetail principal) {
        List<OrderHistoryDTO> list = this.riderService.getOrderHistory("accept", principal.getMember());

        model.addAttribute("list", list);

        return "rider/rider-accept";
    }

    // 배달 목록 리스트 페이지 [배달]
    @GetMapping("/delivery")
    @PreAuthorize("isAuthenticated()")
    public String deliveryView(Model model, @AuthenticationPrincipal RiderSecurityDetail principal) {

        List<OrderHistoryDTO> list = this.riderService.getOrderHistory("delivery", principal.getMember());

        model.addAttribute("list", list);

        return "rider/rider-delivery";
    }

    // 최적의 이동 경로 가져오기 [배달]
    @PostMapping("/ajaxGetKakaoMobilityDirections")
    @ResponseBody
    public Map<String, Object> ajaxGetKakaoMobilityDirections(
            @RequestParam("riderLat") double riderLat,
            @RequestParam("riderLng") double riderLng,
            @RequestParam("destinationLat") double destinationLat,
            @RequestParam("destinationLng") double destinationLng) {
        // TODO: process POST request
        Map<String, Object> result = this.kakaoMobilityService.getDirections(riderLng, riderLat, destinationLng,
                destinationLat);
        return result;
    }

    // 배달 완료 리스트 페이지 [완료]
    @GetMapping("/complete")
    public String completeView(Model model, @AuthenticationPrincipal RiderSecurityDetail principal) {

        List<OrderHistoryDTO> list = this.riderService.getOrderHistory("complete", principal.getMember());
        model.addAttribute("list", list);

        return "rider/rider-complete";
    }

    // 커뮤니티 리스트 페이지 [커뮤니티]
    @GetMapping("/community")
    @PreAuthorize("isAuthenticated()")
    public String communityView(Model model) {
        System.out.println("커뮤니티 이동");
        List<RiderCommunityDTO> list = this.riderService.getList();

        for (RiderCommunityDTO community : list) {
            int likeCount = 0;
            int unlikeCount = 0;

            for (CommunityEmotionsDTO dto : community.getEmotions()) {
                if (dto.getEmotionsType() == 1) {
                    likeCount++;
                } else if (dto.getEmotionsType() == 2) {
                    unlikeCount++;
                }
            }
            community.setLikeCount(likeCount);
            community.setUnlikeCount(unlikeCount);
        }

        model.addAttribute("list", list);

        return "rider/rider-community";
    }

    // 회원가입폼으로 이동을 위한 메서드
    @GetMapping("/insert")
    public String insertForm(RiderInsertForm riderInsertForm) {
        return "rider/rider-register";
    }

    // 회원가입을 위한 메서드
    @PostMapping("/insert")
    public String insertForm(@Valid RiderInsertForm riderInsertForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "rider/rider-register";
        }
        riderService.insertMember(riderInsertForm);
        return "redirect:/rider";
    }

    // ajax 아이디 찾기
    // 필요한 매개변수 : String memberName , String memberEmail
    @PostMapping("/ajaxFindRiderId")
    // return 한 결과가 페이지이동이아닌 단순하게 값전달만 하도록하는 어노테이션
    @ResponseBody
    public String ajaxFindRiderId(@RequestParam("riderName") String memberName,
            @RequestParam("riderEmail") String memberEmail) {
        String riderId = this.riderService.ajaxFindRiderId(memberName, memberEmail);
        return riderId;
    }

    // ajax 비밀번호 찾기
    // 필요한 매개변수 : String memberName , String memberEmail
    @PostMapping("/ajaxFindRiderPw")
    @ResponseBody
    public String ajaxFindRiderPw(@RequestParam("memberId") String memberId,
            @RequestParam("memberEmail") String memberEmail) {
        // TODO: 라이더 아이디 , 이메일로 비밀번호 찾기 (임시비밀번호 발급)
        String result = "false";

        result = this.riderService.ajaxFindRiderPw(memberId, memberEmail);
        System.out.println("result : " + result);
        return result;
    }

    // ajax 아이디 중복확인
    // 필요한 매개변수 : String memberId
    @PostMapping("/ajaxDuplicationId")
    @ResponseBody
    public boolean ajaxDuplicationId(@RequestParam("memberId") String memberId) {
        boolean result = this.riderService.ajaxDuplicationId(memberId);
        // 조회 결과 true / false 로 리턴
        return result;
    }

    // ajax 이메일 중복확인 및 인증번호 전송
    // 필요한 매개변수 : String memberEmail
    @PostMapping("/ajaxDuplicationEmail")
    @ResponseBody
    public String ajaxDuplicationEmail(@RequestParam("memberEmail") String memberEmail) {
        boolean result = this.riderService.ajaxDuplicationEmail(memberEmail);
        System.out.println("result : " + result);
        String response = "";
        if (result == false) {
            response = "NotDuplicationEmail";
            return response;
        }
        try {
            response = mailSend.sendEmailCodeMail(memberEmail, "오늘의빵 라이더 이메일 인증");
        } catch (MessagingException e) {
            // 이메일 전송 실패시
            response = "MailSendFail";
        }
        return response;
    }

    // ajax 전화번호 중복확인
    // 필요한 매개변수 : String memberPhone
    @PostMapping("/ajaxDuplicationPhone")
    @ResponseBody
    public boolean ajaxDuplicationPhone(@RequestParam("memberPhone") String memberPhone) {
        boolean result = this.riderService.ajaxDuplicationPhone(memberPhone);
        return result;
    }

    // 라이더 커뮤니티 글작성
    // 필요한 매개변수 : String communityContent
    @PostMapping("/ajaxCommunityInsert")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public String ajaxCommunityInsert(
            @RequestParam("communityContent") String communityContent,
            @AuthenticationPrincipal RiderSecurityDetail principal) {

        MemberDTO member = principal.getMember();
        System.out.println("작성자 ID: " + member.getMemberId());
        System.out.println("글 내용: " + communityContent);

        // DB에 저장 로직 실행 (Service 호출)
        this.riderService.ajaxCommunityInsert(member, communityContent);

        return "success";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/communityListFragment")
    public String getCommunityListFragment(Model model) {
        List<RiderCommunityDTO> list = this.riderService.getList();
        for (RiderCommunityDTO community : list) {
            int likeCount = 0;
            int unlikeCount = 0;

            for (CommunityEmotionsDTO dto : community.getEmotions()) {
                if (dto.getEmotionsType() == 1) {
                    likeCount++;
                } else if (dto.getEmotionsType() == 2) {
                    unlikeCount++;
                }
            }
            community.setLikeCount(likeCount);
            community.setUnlikeCount(unlikeCount);
        }
        model.addAttribute("list", list);
        return "rider/communityList :: communityList"; // fragment 반환
    }

    @PostMapping("/ajaxCommunityEmotions")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public String ajaxCommunityEmotions(@RequestParam("communityNo") Long communityNo,
            @RequestParam("emotionsType") int emotionsType,
            @AuthenticationPrincipal RiderSecurityDetail principal) {

        // 로그인 된 라이더 정보 꺼내오기
        MemberDTO member = principal.getMember();

        // 라이더 게시글 조회
        RiderCommunityDTO dto = this.riderService.findBycommunityNo(communityNo);
        if (dto == null) {
            return null;
        }
        this.riderService.emotions(member, dto, emotionsType);
        return "success";
    }

    // 게시판의 감정표현의 총 합을 가져오는 ajax
    // 필요한 매개변수 : Long communityNo
    @PostMapping("/ajaxGetEmotionCounts")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public int[] ajaxGetEmotionCountse(@RequestParam("communityNo") Long communityNo) {
        RiderCommunityDTO dto = this.riderService.findBycommunityNo(communityNo);
        if (dto == null) {
            return new int[] { 0, 0 };
        }

        int likeCount = 0;
        int unlikeCount = 0;
        for (CommunityEmotionsDTO emotions : dto.getEmotions()) {
            if (emotions.getEmotionsType() == 1) {
                likeCount++;
            } else if (emotions.getEmotionsType() == 2) {
                unlikeCount++;
            }
        }
        return new int[] { likeCount, unlikeCount };
    }

    // 회원의 정보 (차트) 페이지 [내정보]
    @GetMapping("/chart")
    @PreAuthorize("isAuthenticated()")
    public String chart(@AuthenticationPrincipal RiderSecurityDetail principal, Model model) {

        Map<String, Integer> result = this.riderService.riderChart(principal.getMember());
        model.addAttribute("chart", result);
        return "rider/rider-chart";
    }

    // 회원의 정보 (차트) 페이지 업데이트 [내정보]
    @PostMapping("/ajaxUpdateChart")
    @ResponseBody
    public Map<String, Integer> ajaxUpdateChart(@RequestParam("year") int year, @RequestParam("month") int month,
            @AuthenticationPrincipal RiderSecurityDetail principal) {
        // TODO: process POST request
        Map<String, Integer> result = this.riderService.ajaxUpdateChart(principal.getMember(), year, month);
        return result;
    }

    // API 테이스트
    @GetMapping("/kakaoNavi)")
    public String kakaoNavi() {
        return new String("rider/kakao-navi");
    }

    // 회원의 정보 (차트) 페이지에서 수정 [내정보]
    @PostMapping("/ajaxUpdateMemberInfo")
    @ResponseBody
    public ResponseEntity<String> ajaxUpdateMemberInfo(@RequestBody Map<String, String> params,
            @AuthenticationPrincipal RiderSecurityDetail principal) {
        MemberDTO member = principal.getMember();
        System.out.println("param");
        System.out.println(params);
        System.out.println("==================================================");
        this.riderService.ajaxUpdateMemberInfo(params, member);

        return ResponseEntity.ok("회원 정보 수정 완료");
    }

    @PostMapping("/ajaxSelectDeliveryFee")
    @ResponseBody
    public List<DeliveryFeeDTO> ajaxSelectDeliveryFee(@RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal RiderSecurityDetail principal) {
        MemberDTO member = principal.getMember();
        LocalDateTime now = LocalDateTime.now();
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime startTaget = LocalDateTime.of(year, month, 1, 0, 0, 1);
        LocalDateTime endTaget = ym.atEndOfMonth().atTime(23, 59, 59);
        System.out.println(now.getMonth().equals(month));

        List<DeliveryFeeDTO> request = this.riderService.findTop10ByMemberOrderByCreatedAtDesc(member, startTaget,
                endTaget);

        return request;
    }

    // 임시비밀번호로 로그인시 이동할 페이지
    @GetMapping("/change-password")
    public String changePassword() {
        return "rider/rider-password";
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public String postMethodName(@AuthenticationPrincipal RiderSecurityDetail principal,
            @RequestParam("newPassword") String newPassword, @RequestParam("confirmPassword") String confirmPassword) {
        // TODO: process POST request
        if (newPassword.equals(confirmPassword)) {
            this.riderService.changePassword(principal.getMember(), newPassword);
        }

        return "redirect:/rider/main";
    }

    // 회원정보 DB에서 새로고침해서 세션(Security에 넣기)
    @PostMapping("/authenticatoinUpdate")
    @PreAuthorize("isAuthenticated()")
    public String authenticatoinUpdate(@AuthenticationPrincipal RiderSecurityDetail principal,
            @RequestParam("memberNo") long memberNo) {
        // TODO: process POST request
        String response = "false";
        if (principal.getMember().getMemberNo() == memberNo) {
            UserDetails updatedUser = userDetailsService.loadUserByUsername(principal.getMember().getMemberId());
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUser,
                    updatedUser.getPassword(),
                    updatedUser.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(newAuth);
            response = "success";
        }
        return response;
    }

    // @GetMapping("/dummyRider")
    // public String dummyRider() {
    // this.riderService.dummyRider();
    // return "redirect:/";
    // }

}
