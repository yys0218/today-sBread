package com.ex.member.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ex.admin.model.data.RestrictDTO;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.data.MemberInsertForm;
import com.ex.member.model.service.MemberService;
import com.ex.admin.model.service.RestrictService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

//	MemberController
//	- 역할 : 회원가입 / 로그인 / 로그아웃 / 아이디,비번 찾기 / 마이페이지 / 회원정보 수정/탈퇴 등 "회원" 관련 웹 요청을 처리하는 컨트롤러
@Controller
@RequiredArgsConstructor
@RequestMapping("/member/*") // 기본 URL : http://localhost:8080/member/
public class MemberController {
	// 서비스 객체 주입 (회원 관련 비즈니스 로직)
	private final MemberService memberService;
	private final RestrictService restrictService;

	// ===== 세션 테스트용 =====
	// 모든 컨트롤러 요청이 처리되기 전에 실행되는 메서드
	// 세션에 user 정보가 없으면 임시로 테스트용 사용자 아이디를 설정하여 항상 로그인된 상태처럼 동작하게 만든다.
	// *실제 서비스 시에는 제거 해야 함.*
//    @ModelAttribute
//    public void setTestUser(HttpSession session) {
//        if(session.getAttribute("user") == null) {
//            MemberDTO testDto = new MemberDTO();
//            testDto.setMemberId("testUser");
//            testDto.setMemberName("테스트");
//            testDto.setMemberEmail("test@test.com");
//            session.setAttribute("user", testDto);	// 임의의 테스트용 아이디
//        }
//    }

	// [GET] 회원가입 화면
	// - URL : http://localhost:8080/member/signup
	// - Model : "memberInsertForm"
	@GetMapping("signup")
	public String signupForm(@ModelAttribute("memberInsertForm") MemberInsertForm form) {
		return "member/signupForm";
	}

	// [POST] 회원가입 처리
	// - URL : http://localhost:8080/member/signup
	// - Param : MemberInsertForm (+ BindingResult)
	// 폼에서 입력한 값(MemberInsertForm)을 받아 검증 후 회원가입 시도
	// 성공 : 로그인 페이지로 리다이렉트
	// 실패 : 에러 메시지와 함께 다시 회원가입 화면으로
	@PostMapping("signup")
	public String signup(@ModelAttribute("memberInsertForm") MemberInsertForm form, BindingResult bindingResult,
			Model model, RedirectAttributes ra) {

		// 이메일 합치기 : 화면은 local/domain을 나눠 받으므로 서버에서 "local@domain"으로 합침
		if (form.getEmailLocal() != null && !form.getEmailLocal().isEmpty() && form.getEmailDomain() != null
				&& !form.getEmailDomain().isEmpty()) {
			form.setMemberEmail(form.getEmailLocal() + "@" + form.getEmailDomain());
		} else {
			// 화면에 보여줄 필드 에러 등록
			bindingResult.rejectValue("memberEmail", "NotEmpty", "이메일은 필수 입력 항목입니다.");
		}
		// 비밀번호 일치 확인
		if (form.getMemberPw() == null || !form.getMemberPw().equals(form.getMemberPw1())) {
			bindingResult.rejectValue("memberPw1", "passwordMismatch", "비밀번호가 일치하지 않습니다.");
		}
		// 전화번호 유효성 검사 (010으로 시작, 숫자만 10~11자리)
		String phone = form.getMemberPhone();
		if (phone == null || !phone.matches("^01[0-9]{8,9}$")) {
		    bindingResult.rejectValue("memberPhone", "invalidPhone", "전화번호 형식이 올바르지 않습니다. (예: 01012345678)");
		}

		// 위에서 추가한 폼 오류가 있으면 즉시 폼 화면으로 돌아감
		if (bindingResult.hasErrors()) {
			bindingResult.getFieldErrors()
					.forEach(err -> System.out.printf(" - field=%s, rejected=%s, code=%s, msg=%s%n", err.getField(),
							err.getRejectedValue(), err.getCode(), err.getDefaultMessage()));
			bindingResult.getGlobalErrors().forEach(err -> System.out.printf(" - object=%s, code=%s, msg=%s%n",
					err.getObjectName(), err.getCode(), err.getDefaultMessage()));

			model.addAttribute("alertMessage", "입력값을 확인해주세요.");
			model.addAttribute("alertType", "error");
			return "member/signupForm"; // 에러가 있으면 같은 화면으로 이동
		}

		// 서비스 호출하여 실제 회원가입 시도
		try {
			memberService.register(form); // 내부적으로 아이디/이메일/닉네임 중복 체크 + 암호화 + 저장
		} catch (IllegalArgumentException e) {
			// 서비스에서 "검증 실패"로 던진 예외
			e.printStackTrace();

			// 화면에도 에러 표시
			bindingResult.reject("memberSignupError", e.getMessage());
			model.addAttribute("alertMessage", e.getMessage());
			model.addAttribute("alertType", "error");
			return "member/signupForm";
		} catch (org.springframework.dao.DataIntegrityViolationException ex) {
			// DB 유니크 제약 위반 시 뜨는 에러 메시지
			bindingResult.reject("memberSignupError", "이미 사용중인 정보가 있습니다. 아이디/이메일/전화번호를 확인해주세요.");
			model.addAttribute("alertMessage", "이미 사용중인 정보가 있습니다. 아이디/이메일/전화번호를 확인해주세요.");
			model.addAttribute("alertType", "error");
			return "member/signupForm";
		} catch (Exception ex) {
			// 그 외 예기치 못한 오류
			bindingResult.reject("memberSignupError", "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도하세요.");
			model.addAttribute("alertMessage", "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도하세요.");
			model.addAttribute("alertType", "error");
			return "member/signupForm";
		}

		// 회원가입 성공 시 로그인 페이지로 이동
		ra.addFlashAttribute("alertMessage", "회원가입이 완료되었습니다!");
		ra.addFlashAttribute("alertType", "success");
		return "redirect:/member/login";
	}

	// [POST] 아이디 중복 확인 (AJAX)
	// - URL : http://localhost:8080/member/checkId
	// 파라미터 : { "memberId" : "입력값" }
	// 반환 : { "available" : true/false }
	// 화면에서 즉시 사용 가능 여부 표시
	@PostMapping("checkId")
	@ResponseBody
	public Map<String, Boolean> checkId(@RequestBody Map<String, String> request) {
		String memberId = request.get("memberId");
		boolean available = memberService.isIdAvailable(memberId); // service에서 사용 가능 여부 체크
		return Map.of("available", available);
	}

	// [POST] 이메일 중복 확인 (AJAX)
	// - URL : http://localhost:8080/member/checkEmail
	// 파라미터 : { "memberEmail" : "입력값" }
	// 반환 : { "available" : true/false }
	@PostMapping("checkEmail")
	@ResponseBody
	public Map<String, Boolean> checkEmail(@RequestBody Map<String, String> request) {
		String email = request.get("memberEmail");
		boolean available = memberService.isEmailAvailable(email); // service에서 사용 가능 여부 체크
		return Map.of("available", available);
	}

	// [POST] 닉네임 중복 확인 (AJAX)
	// - URL : http://localhost:8080/member/checkNick
	// 파라미터 : { "memberNick" : "입력값" }
	// 반환 : { "available" : true/false }
	@PostMapping("checkNick")
	@ResponseBody
	public Map<String, Boolean> checkNick(@RequestBody Map<String, String> request) {
		String nick = request.get("memberNick");
		boolean available = memberService.isNickAvailable(nick);
		return Map.of("available", available);
	}

	// [GET] 로그인 화면
	// URL : http://localhost:8080/member/login
	// 이미 로그인이 되어 있으면 메인으로 보냄
	// 자동 로그인 쿠키("autoLogin")가 존재하면 토큰으로 사용자 조회 -> 세션 저장 -> 메인 이동
	@GetMapping("login")
	public String login(HttpServletRequest request, HttpSession session, RedirectAttributes ra) {

		// 이미 세션에 로그인 정보가 있으면 메인 페이지로 이동
		if (session.getAttribute("user") != null) {
			ra.addFlashAttribute("alertMessage", "이미 로그인되어 있습니다.");
			ra.addFlashAttribute("alertType", "info");
			return "redirect:/";
		}

		// 자동 로그인 쿠키 확인
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				// 쿠키 이름 = "autoLogin"
				if ("autoLogin".equals(cookie.getName())) {
					String token = cookie.getValue();

					// DB에서 토큰에 해당하는 사용자 조회
					MemberDTO dto = memberService.getMemberByAutoLoginToken(token);

					// 탈퇴 계정(memberRole = 3) 이라면 자동 로그인을 하지 않음
					// DB 조회 시 null 이면 토큰이 무효이므로 자동 로그인 생략
					if (dto != null && dto.getMemberRole() != 3) {

						// 활성 회원이면 세션에 저장하여 자동 로그인 처리
						session.setAttribute("user", dto);
						ra.addFlashAttribute("alertMessage", "자동 로그인되었습니다.");
						ra.addFlashAttribute("alertType", "success");
						return "redirect:/"; // 자동 로그인 성공 시 메인 페이지로 이동
					}
					break; // "autoLogin" 쿠키 처리 후 루프 종료
				}
			}
		}

		// 로그인 성공 후 돌아갈 "이전 페이지" 저장 (로그인/회원가입 페이지이면 저장하지 않음)
		String referer = request.getHeader("Referer");
		if(referer != null	// 이전 페이지 정보가 null이 아니고, 값이 있을 때만 작동
			&& !referer.contains("/member/login")	// 로그인 페이지가 아닐 때
			&& !referer.contains("/member/signup")){	// 회원가입 페이지가 아닐 때
			session.setAttribute("returnURL", referer);	// 위 조건을 모두 만족할 때만 이전 페이지 저장함
		}else {
			session.removeAttribute("returnURL");		// 이전 페이지가 로그인/회원가입 페이지 이면 이전 페이지 세션을 지움
		}
		return "member/login";	// 로그인 페이지로 이동
	}

	// [POST] 로그인 처리
	// URL : http://localhost:8080/member/login
	// 파라미터 : memberId, memberPw, autoLogin (on 체크시)
	// 탈퇴 계정 (memberRole = 3)은 비밀번호가 맞아도 로그인 불가
	// 성공 : 세션 저장 -> (임시비번이면 /member/change-pw, 아니면 이전 페이지 또는 메인)
	// 실패 : 로그인 화면으로 에러 메시지
	@PostMapping("login")
	public String login(@RequestParam(name = "memberId") String memberId,
			@RequestParam(name = "memberPw") String memberPw,
			@RequestParam(name = "autoLogin", required = false) String autoLogin, // 체크박스 선택 여부 - 자동 로그인
			HttpServletResponse response, // 쿠키 설정을 위해 사용
			HttpSession session, // 세션을 사용자 정보 저장
			Model model, RedirectAttributes ra) {

		memberPw = memberPw.trim(); // 비밀번호 앞뒤 공백 제거

		// 제재 계정이면 별도 메시지로 즉시 차단
		// -getMember() 는 탈퇴 포함 일반 조회 -> memberRole = -1 이면
		// 제재 기간 경과여부 확인 후, 미경과 시 로그인 안됨
		MemberDTO any = memberService.getMember(memberId);
		if (any != null && any.getMemberRole() == -1) {
			int memberNo = any.getMemberNo();
			if (restrictService.checkRestrict(memberNo)) {
			model.addAttribute("title", "정지 해제.");
			model.addAttribute("msg", "제재 기간이 만료되었습니다. \n 다시 로그인 해주세요.");
			model.addAttribute("icon", "success");
			model.addAttribute("loc", "/member/login");
			return "common/msg"; // 메인으로 이동하기 전 알림 띄우기
			}
			RestrictDTO restrict = restrictService.getRestrict(memberNo);
			int reason = restrict.getRestrictReason();
			String stringReason;
			if (reason == 0) {
				stringReason = "운영방침 위반";
			} else if (reason == 1) {
				stringReason = "부적절한 이용행위";
			} else if (reason == 2) {
				stringReason = "결제관련 문제";
			} else if (reason == 3) {
				stringReason = "법적 문제";
			} else {
				stringReason = "판매자 피해 누적";
			}
			String type = restrict.getRestrictType();
			LocalDate period = restrict.getRestrictPeriod();
			model.addAttribute("title", "정지 계정입니다.");
			model.addAttribute("msg", stringReason + "사유로"+ period + "까지" + type + "정지 되었습니다.");
			model.addAttribute("icon", "error");
			model.addAttribute("loc", "/member/login");
			return "common/msg"; // 메인으로 이동하기 전 알림 띄우기
		}

		// 탈퇴 계정이면 별도 메시지로 즉시 차단
		// -getMember() 는 탈퇴 포함 일반 조회 -> memberRole = 3 이면 탈퇴 계정으로 안내
		if (any != null && any.getMemberRole() == 3) {
			ra.addFlashAttribute("alertMessage", "회원 탈퇴 계정입니다.");
			ra.addFlashAttribute("alertType", "error");
			return "redirect:/member/login";
		}

		// DB에서 사용자 정보 조회
		MemberDTO dto = memberService.getMember(memberId);

		// 비밀번호 일치 확인 => 활성 회원만 대상으로 함
		// 내부적으로 Repository의 findActiveById 사용 (memberRole <> 3)
		boolean match = memberService.checkActivePassword(memberId, memberPw);

		if (!match) {
			// 아이디 없음 / 탈퇴 / 비번 불일치 모두 같은 메시지 (정보 노출 방지)
			ra.addFlashAttribute("alertMessage", "아이디 또는 비밀번호가 올바르지 않습니다.");
			ra.addFlashAttribute("alertType", "warning");
			return "redirect:/member/login";
		}

		// 로그인 성공
		MemberDTO loginUser = memberService.getActiveMember(memberId); // 최신 정보
		session.setAttribute("user", loginUser);

		// 임시 비번이면 비번 변경 페이지로 안내
		if (memberService.isTemporaryPassword(loginUser)) {
			ra.addFlashAttribute("alertMessage", "임시 비밀번호로 로그인되었습니다. 비밀번호를 변경해주세요.");
			ra.addFlashAttribute("alertType", "info");
			return "redirect:/member/change-pw";
		}

		// @PostMapping("login")
		// 자동 로그인 요청 시 : 토큰 발급 -> DB 저장 -> 쿠키 생성
		if ("on".equals(autoLogin)) {
			String autoLoginToken = UUID.randomUUID().toString(); // 랜덤 토큰 발급
			memberService.saveAutoLoginToken(memberId, autoLoginToken); // DB 저장

			Cookie cookie = new Cookie("autoLogin", autoLoginToken); // 쿠키 생성
			cookie.setPath("/"); // 사이트 전체에서 사용 가능
			cookie.setMaxAge(60 * 60 * 24 * 7); // 7일간 유지
			cookie.setHttpOnly(true); // JS에서 접근 불가, 보안 강화

			// 응답에 쿠키 추가
			response.addCookie(cookie);
		}

		// 이전 페이지 복귀 (단, 회원가입 페이지로는 절대 돌아가지 않음)
		String returnURL = (String) session.getAttribute("returnURL"); // 세션에서 이전 페이지 주소 꺼내기
		session.removeAttribute("returnURL"); // 세션에서 이전 페이지의 주소 삭제

		// 이전 페이지가 없거나, 로그인 / 회원가입 페이지에서 온 경우는 메인 페이지로 이동
		if(returnURL == null || returnURL.isBlank() || returnURL.contains("/member/signup") || returnURL.contains("/member/login")) {
			ra.addFlashAttribute("alertMessage", "로그인되었습니다.");
			ra.addFlashAttribute("alertType", "success");
			return "redirect:/";
		}

		// 정상적으로 이전 페이지로 이동
		ra.addFlashAttribute("alertMessage", "로그인되었습니다!");
		ra.addFlashAttribute("alertType", "success");
		return "redirect:" + returnURL; // 이전 페이지로 이동

	}

	// [GET] 로그아웃
	// URL : http://localhost:8080/member/logout
	// 세션 삭제 후 메인 페이지로 이동
	@GetMapping("logout")
	public String logout(HttpSession session, HttpServletRequest request) {
		session.invalidate(); // 세션 전체 삭제 (로그아웃)
		return "redirect:/?logout=1"; // 메인으로 이동
	}

	// [GET] 아이디 찾기 화면
	@GetMapping("find-id")
	public String findId() {
		return "member/find-id";
	}

	// [POST] 아이디 찾기 (AJAX)
	// URL : http://localhost:8080/member/find-id
	// 요청 바디(JSON) : {memberName, memberEmail }
	// 응답 바디(JSON) : { success, membId? | message? }
	// success=true -> { success:true, memberId: "찾은 아이디" }
	// success=false -> { success:false, message: "일치하는 회원이 없습니다." }
	@PostMapping("find-id")
	@ResponseBody
	public Map<String, Object> findId(@RequestBody Map<String, String> payload) {
		String memberName = payload.get("memberName");
		String memberEmail = payload.get("memberEmail");

		Map<String, Object> result = new HashMap<>();
		MemberDTO dto = memberService.findMemberId(memberName, memberEmail);

		if (dto != null) {
			result.put("success", true);
			result.put("memberId", dto.getMemberId());
		} else {
			result.put("success", false);
			result.put("message", "일치하는 회원이 없습니다.");
		}
		return result;
	}

	// [GET] 비밀번호 찾기 화면
	@GetMapping("find-pw")
	public String findPw() {
		return "member/find-pw";
	}

	// [POST] 비밀번호 찾기 (AJAX)
	// URL(POST) : http://localhost:8080/member/find-pw
	// 파라미터 : { memberId, memberName, memberEmail }
	// 반환 : { success: true/false, message : "결과 메시지" }
	// 로직 : 일치하는 회원 찾기 -> 임시 비번 생성 -> DB 저장(암호화 + isTemporary=1) -> 이메일 발송
	@PostMapping("find-pw")
	@ResponseBody // 리턴값을 JSON 형태로 응답
	public Map<String, Object> findPw(@RequestBody Map<String, String> payload) {
		// payload : AJAX에서 보낸 데이터 (memberId, memberName, memberEmail)
		String memberId = payload.get("memberId"); // 입력한 아이디
		String memberName = payload.get("memberName"); // 입력한 이름
		String memberEmail = payload.get("memberEmail"); // 입력한 이메일

		Map<String, Object> result = new HashMap<>(); // 결과를 담아서 변환할 Map

		// DB에서 입력한 정보와 일치하는 회원 찾기
		MemberDTO dto = memberService.findMemberByIdNameEmail(memberId, memberName, memberEmail);

		if (dto != null) { // 회원 정보가 존재하면
			// 임시 비밀번호 생성 (랜덤 8자리)
			String tempPw = memberService.generateTempPassword();

			// DB에 임시 비밀번호로 업데이트 (암호화 포함) + isTemporary = 1로 업데이트
			memberService.updatePassword(dto.getMemberId(), tempPw);

			// 이메일로 임시 비밀번호 전송
			memberService.sendTempPasswordEmail(memberEmail, tempPw);

			// AJAX에게 성공 결과 반환
			result.put("success", true);
			result.put("message", "임시 비밀번호가 이메일로 발송되었습니다.");

		} else { // 회원 정보가 없으면
			// AJAX에게 실패 메시지 반환
			result.put("success", false);
			result.put("message", "입력한 정보와 일치하는 회원이 없습니다.");
		}

		// 최종 결과를 JSON 형태로 반환
		return result;
	}

	// [GET] 비밀번호 변경 화면
	// URL : http://localhost:8080/member/change-pw
	// 반환 : 비밀번호 변경 화면 (/member/change-pw.html)
	// 임시 비밀번호로 로그인한 사용자가 접근하는 페이지
	// 로그인 안되어 있으면 로그인 페이지로 보냄
	@GetMapping("change-pw")
	public String changePwForm(HttpSession session, Model model) {
		// 로그인 확인
		MemberDTO loginUser = (MemberDTO) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/member/login";
		}
		model.addAttribute("memberId", loginUser.getMemberId());
		return "member/change-pw";
	}

	// [POST] 비밀번호 변경 처리 (AJAX)
	// URL : http://localhost:8080/member/change-pw
	// 파라미터 : JSON { currentPw, newPw, confirmPw }
	// 반환 : JSON { success: true/false, message: "결과 메시지" }
	// 로그인 상태 필수, 새 비밀번호 저장 후 isTemporary=0 으로 변경
	@PostMapping("change-pw")
	@ResponseBody
	public Map<String, Object> changePassword(@RequestBody Map<String, String> payload, HttpSession session) {

		Map<String, Object> result = new HashMap<>();

		// 로그인 확인
		MemberDTO loginUser = (MemberDTO) session.getAttribute("user");
		if (loginUser == null) {
			result.put("success", false);
			result.put("message", "로그인이 필요합니다.");
			return result;
		}

		String memberId = loginUser.getMemberId();
		String newPw = payload.get("newPw");
		String confirmPw = payload.get("confirmPw");

		// 새 비밀번호와 확인 일치 여부 확인 (프론트에서도 체크하지만 서버에서도 확인)
		if (!newPw.equals(confirmPw)) {
			result.put("success", false);
			result.put("message", "새 비밀번호가 일치하지 않습니다.");
			return result;
		}

		// 비밀번호 변경 서비스 호출
		try {
			// Service 안에서 isTemporary 값을 0으로 갱신
			memberService.changePassword(memberId, newPw);

			// 세션에 반영
			loginUser.setIsTemporary(0);
			session.setAttribute("user", loginUser);

			result.put("success", true);
			result.put("message", "비밀번호가 성공적으로 변경되었습니다.");
		} catch (Exception e) {
			e.printStackTrace(); // 디버깅용 로그
			result.put("success", false);
			result.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
		}
		return result;
	}

	// [GET] 마이페이지 메인
	// URL : http://localhost:8080/member/order-list
	// 반환 : 마이페이지 화면 (member/order-list)
	// 로그인 필수(유효성 검사)
	@GetMapping("mypage")
	public String mypage(HttpSession session, Model model) {
		// 세션에서 user를 꺼낼 때 ClassCastException 방지
		Object userObj = session.getAttribute("user");
		MemberDTO dto = null;
		if (userObj instanceof MemberDTO) {
			dto = (MemberDTO) userObj;
		}
		if (dto == null) {
			return "redirect:/member/login"; // MemberDTO가 아니거나 세션이 없으면 로그인 페이지
		}
		// 화면에서 ${memberName} 사용
		model.addAttribute("memberName", dto.getMemberName());
		// 기본으로 로드할 프래그먼트 지정
		model.addAttribute("contentFragment", "member/account-info :: account-info-form");
		return "redirect:/member/order-list";
	}

	// [GET] 계정 정보 확인 (비밀번호 재확인 폼)
	// URL(GET) : http://localhost:8080/member/account-info
	// 로그인 필수, 화면에 memberId 넘겨서 비밀번호 확인 폼 렌더링
	@GetMapping("account-info")
	public String accountInfo(HttpSession session, Model model) {
		MemberDTO dto = (MemberDTO) session.getAttribute("user");
		if (dto == null) {
			return "redirect:/member/login";
		}
		model.addAttribute("memberId", dto.getMemberId());
		model.addAttribute("error", "");
		return "member/account-info";
	}

	// [POST] 계정 정보 확인 처리 (비밀번호 확인)
	// URL(POST) : http://localhost:8080/member/account-info
	// 파라미터 : memberId, memberPw
	// 현재 비밀번호가 맞으면 상세 정보 페이지로, 아니면 에러와 함께 폼으로 이동
	@PostMapping("account-info")
	public String checkPassword(@RequestParam("memberId") String memberId, @RequestParam("memberPw") String memberPw,
			Model model, HttpSession session, RedirectAttributes ra) {
		// 로그인 확인
		MemberDTO loginUser = (MemberDTO) session.getAttribute("user");
		if (loginUser == null) { // 로그인 안 된 상태
			return "redirect:/member/login";
		}

		// 비밀번호 검증
		boolean valid = memberService.checkPassword(memberId, memberPw);
		if (valid) {
			// 상세 조회 후 화면에 바인딩
			MemberDTO member = memberService.getMember(memberId);
			model.addAttribute("dto", member);
			return "member/account-info-detail";
		} else {
			// 실패 시 에러 메시지와 함께 폼으로 이동
			ra.addFlashAttribute("alertMessage", "비밀번호가 일치하지 않습니다.");
	        ra.addFlashAttribute("alertType", "error");
	        return "redirect:/member/account-info";
		}
	}

	// [GET] 회원 정보 수정 화면
	// URL : http://localhost:8080/member/account-info-update
	// 로그인 필수, 세션 사용자 정보를 dto로 담아 폼에 미리 채워줌
	@GetMapping("account-info-update")
	public String updateMember(HttpSession session, Model model) {
		MemberDTO dto = (MemberDTO) session.getAttribute("user");
		if (dto == null) {
			return "redirect:/member/login";
		}
		model.addAttribute("dto", dto);
		return "member/account-info-update";
	}

	// [POST] 회원 정보 수정 처리
	// URL : http://localhost:8080/member/account-info-update
	// 파라미터 : MemberDTO(dto), memberPw(현재 비번)
	// 본인 확인 (세션 사용자 == 수정 대상) + 현재 비번 검증 후 수정
	// 성공 시 세션도 최신 정보로 갱신
	@PostMapping("account-info-update")
	public String updateMember(MemberDTO dto, @RequestParam("memberPw") String memberPw,
							   @RequestParam("newPw") String newPw,
							   @RequestParam("confirmPw") String confirmPw,
							   HttpSession session,
							   RedirectAttributes ra,
							   Model model) {
		
		// 본인 여부 확인
		MemberDTO loginUser = (MemberDTO) session.getAttribute("user");
		if (loginUser == null || !loginUser.getMemberId().equals(dto.getMemberId())) {
			ra.addFlashAttribute("alertMessage", "권한이 없습니다.");
	        ra.addFlashAttribute("alertType", "error");
			return "redirect:/member/account-info-update";
		}
		
		// 전화번호 중복 유효성 검사
		MemberDTO found = memberService.getMemberByPhone(dto.getMemberPhone());
		if(found != null && !found.getMemberId().equals(dto.getMemberId())) {
			ra.addFlashAttribute("alertMessage","이미 다른 회원이 사용 중인 전화번호입니다.");
			ra.addFlashAttribute("alertType","warning");
			return "redirect:/member/account-info-update";
		}
		
		// 현재 비밀번호 검증
		if (!memberService.checkPassword(dto.getMemberId(), memberPw)) {
			ra.addFlashAttribute("alertMessage", "현재 비밀번호가 올바르지 않습니다.");
	        ra.addFlashAttribute("alertType", "error");
			return "redirect:/member/account-info-update";
		}
		
		// 새 비번 검증
		if(newPw != null && !newPw.isBlank()) {
			if(!newPw.equals(confirmPw)) {
				ra.addFlashAttribute("alertMessage", "새 비밀번호가 일치하지 않습니다.");
	            ra.addFlashAttribute("alertType", "error");
				return "redirect:/member/account-info-update";
			}
			// 새 비밀번호로 덮어쓰기
			dto.setMemberPw(newPw);
		}
		
		// 서비스 호출로 수정 (이름/전화번호/새 비번 등)
		memberService.updateMember(dto);

		// 세션의 사용자 정보도 최신으로 갱신
		session.setAttribute("user", memberService.getMember(dto.getMemberId()));

		ra.addFlashAttribute("alertMessage", "회원 정보가 성공적으로 수정되었습니다.");
	    ra.addFlashAttribute("alertType", "success");
		
		// 수정 결과 상세 페이지로 이동
		return "redirect:/member/account-info";
	}

	// 공통 공급자 : 탈퇴 사유 옵션
	@ModelAttribute("reasonsOptions")
	public List<String> reasonsOptions() {
		return List.of("서비스 이용이 불편함", "원하는 제품/빵집이 없음", "가격/혜택 불만", "웹 오류 잦음", "배송 불만");
	}

	// [GET] 회원 탈퇴 화면
	// URL : http://localhost:8080/member/withdraw
	// 탈퇴 사유 체크박스/약관동의 폼을 렌더링함
	@GetMapping("withdraw")
	public String withdrawForm(HttpSession session, Model model) {
		// 로그인 확인
		MemberDTO loginUser = (MemberDTO) session.getAttribute("user");
		if (loginUser == null) {
			// 로그인 안되어 있으면 로그인 페이지로 이동
			return "redirect:/member/login";
		}

		// 화면에서 ${memberId} 로 쓸 수 있게 바인딩
		model.addAttribute("memberId", loginUser.getMemberId());
		model.addAttribute("withdrawError", "");

		// 탈퇴 폼 페이지로 이동
		return "member/withdraw";
	}

	// [POST] 회원 탈퇴 처리
	// URL(POST) : http://localhost:8080/member/withdraw
	// 파라미터 : memberId, reasons(라디오 버튼체크박스), otherReason(기타 사유), agree(약관 동의)
	// 본인 확인 -> 유효성 검사(사유 1개 이상 or 기타) && 약관 동의 -> 사유 묶어서 문자열로 만들기 -> 서비스
	// 호출(withdrawMember(memberId, reasonSummary)) -> 세션 무효화 후 메인으로 이동
	@PostMapping("withdraw")
	public String withdraw(@RequestParam("memberId") String memberId,
			@RequestParam(value = "reason", required = false) String reason, // 단일 선택
			@RequestParam(value = "otherReason", required = false) String otherReason, // "기타" 입력
			@RequestParam(value = "agree", required = false) String agree, // 동의 체크
			HttpSession session, Model model) {
		// 로그인 확인
		MemberDTO loginUser = (MemberDTO) session.getAttribute("user");
		if (loginUser == null) {
			return "redirect:/member/login";
		}

		// 본인 계정인지 확인
		if (!loginUser.getMemberId().equals(memberId)) {
			// 에러 메시지와 함께 다시 탈퇴 폼으로
			model.addAttribute("withdrawError", "권한이 없습니다. 다시 시도해 주세요.");
			// 폼 재렌더링 시 필요한 값들 다시 바인딩
			model.addAttribute("memberId", loginUser.getMemberId());
			return "member/withdraw";
		}

		// 유효성 검사 : 사유 선택 + (기타: 텍스트)
		boolean selected = (reason != null);
		boolean isOther = selected && "기타".equals(reason);
		boolean hasOther = otherReason != null && !otherReason.trim().isEmpty();
		boolean agreed = (agree != null);

		if (!selected || (isOther && !hasOther)) {
			model.addAttribute("withdrawError", "탈퇴 사유를 선택하세요. (기타 선택 시 내용 입력)");
			model.addAttribute("memberId", loginUser.getMemberId());
			model.addAttribute("selectedReason", reason);
			model.addAttribute("otherReason", otherReason == null ? "" : otherReason);
			model.addAttribute("agreeChecked", agreed);
			return "member/withdraw";
		}
		if (!agreed) {
			model.addAttribute("withdrawError", "탈퇴 시 유의사항에 동의해야 탈퇴를 진행할 수 있습니다.");
			model.addAttribute("memberId", loginUser.getMemberId());
			model.addAttribute("selectedReason", reason);
			model.addAttribute("otherReason", otherReason == null ? "" : otherReason);
			model.addAttribute("agreeChecked", false);
			return "member/withdraw";
		}

		// 사유 요약
		String reasonSummary = isOther ? "기타: " + otherReason.trim() : reason;

		// 서비스 호출
		memberService.withdrawMember(memberId, reasonSummary);

		// 세션 종료 후 메인
		session.invalidate();
		return "redirect:/?withdraw=1";
	}
}