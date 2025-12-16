package com.ex.member.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ex.member.model.data.AddressBookDTO;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.service.AddressBookService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
// 마이페이지 > 배송지 관리 컨트롤러

/*	URL 베이스 /member/*
 	/member/address 				: 배송지 관리 메인 (목록/기본 배송지 노출)
 	/member/address-add 			: 배송지 추가 (GET : 폼 화면 / POST : 저장)
 	/member/address/set-default 	: 기본 배송지 설정(POST)
*/
@Controller
@RequiredArgsConstructor
@RequestMapping("/member/*") // 기본 URL : http://localhost:8080/member/*
public class AddressBookController {
	private final AddressBookService addressBookService;

	// [GET]마이페이지 -> 배송지 관리 화면
	// 세션에서 로그인 회원(MemberDTO)을 꺼냄. (세션 키 : "user")
	// 해당 회원의 "기본 배송지 1건"을 화면에 노출
	// 배송지가 없다면, 그 자리에서 회원가입 정보로 자동 생성 후 노출
	@GetMapping("address")
	public String address(@SessionAttribute(name="user", required=false) MemberDTO member, Model model) {
		
		if(member == null) {return "redirect:/member/login";}
		
		// 회원의 기본 배송지 1건 조회
		AddressBookDTO address = addressBookService.getMyOnlyAddress(member);
		
		// 배송지가 없으면 바로 생성 (회원가입 주소로 1건 생성 + 기본 Y) 후 다시 조회
		if(address == null) {
			addressBookService.createDefaultAddressFromSignup(member);
			address = addressBookService.getMyOnlyAddress(member);
		}
		
		// 전체 리스트 (기본 Y 우선 정렬)도 함께 내려주기
		List<AddressBookDTO> addresses = addressBookService.getAllAddresses(member.getMemberId());
		
		// 뷰로 데이터 전달 : 모델에 address 키로 담아서 보낸다.
		// 뷰에서는 ${address.receiverName}, ${address.receiverPhone}, ${address.addressDetail} 로 사용
		model.addAttribute("address", address); 					// 단일 기본 배송지
		model.addAttribute("addresses", addresses);					// 전체 목록
		model.addAttribute("maxReached", addresses.size() >= 5);	// 최대 5건 제한 알림용
		
		return "member/address"; 	// member/address.html
	}
	
	// [GET] 배송지 추가 폼 화면
	// 폼 렌더링, 저장은 POST에서 처리
	// 뷰 파일명 : address-add.html -> return "member/address-add";
	@GetMapping("address-add")
	public String showAddform(@SessionAttribute(name="user", required=false) MemberDTO member, Model model) {
		
		if(member == null) {return "redirect:/member/login";}
		return "member/address-add";
	}
	
	// [POST] 배송지 추가
	// 화면 하단의 배송지 추가 폼에서 전송되는 값을 받아 저장
	// 추가되는 주소는 기본값이 아님(N). 기본(Y)은 한 개만 유지
	// 저장 후 다시 /member/address 로 이동하여 화면 갱신
	@PostMapping({"address-add", "address/add"})
	public String addAddress( @SessionAttribute(name = "user", required = false) MemberDTO member,
							  @RequestParam("receiverName") String receiverName,
							  @RequestParam("receiverPhone") String receiverPhone,
							  @RequestParam("addressDetail") String addressDetail,
							  RedirectAttributes redirectAttributes) {		
		// 로그인 체크
		if(member == null) {return "redirect:/member/login";}
		
		try {
		// 추가 주소는 기본 N
		addressBookService.addSubAddress(member, receiverName, receiverPhone, addressDetail);
		redirectAttributes.addFlashAttribute("msg","배송지가 추가되었습니다.");
		}catch(IllegalStateException e) {
			// 5개 초과 시 안내 메시지
			redirectAttributes.addFlashAttribute("error", e.getMessage());
		}
		
		// 저장 후 목록 리다이렉트
		return "redirect:/member/address";
	}
	
	// 배송지 삭제
	@PostMapping("address/delete")
	public ResponseEntity<?> deleteAddress(@SessionAttribute(name="user", required=false) MemberDTO member,
										   @RequestParam("addressNo") int addressNo){
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        AddressBookDTO target = addressBookService.getOneForOwner(addressNo, member.getMemberId());
        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않거나 권한이 없는 주소입니다.");
        }

        try {
            addressBookService.deleteAddress(addressNo, member.getMemberId());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) { // ex) 마지막 1건은 삭제 불가 등
            return ResponseEntity.badRequest().body(e.getMessage());
        }
	}
	
	// [POST] 기본 배송지 설정 (라디오 버튼으로 선택)
	// 주소 리스트에서 라디오 버튼으로 선택된 addressNo를 받음
	// 해당 주소를 기본(Y)으로, 나머지는 기본(N)으로 변경함
	// 완료 후 다시 /member/address로 이동
	// 라디오 사용 예시) <input type="radio" name="addressNo" th:value="${addr.addressNo}" th:checked="${addr.isDefault == 'Y'}" />
	@PostMapping("address/set-default")
	public String setDefaultAddress(@SessionAttribute(name = "user", required = false) MemberDTO member,
									@RequestParam("addressNo") int addressNo) {		
		// 로그인 체크
		if(member == null) {return "redirect:/member/login";}
		
		// 서비스에서 할 일 : 선택 주소만 기본 Y, 나머지는 N으로 처리
		addressBookService.setDefaultAddress(addressNo, member.getMemberId());
		
		return "redirect:/member/address";
	}
	
	// 배송지 수정
	@GetMapping("address/edit")
	public String editAddressForm(@SessionAttribute(name="user", required=false) MemberDTO member,
								  @RequestParam("addressNo") int addressNo,
								  Model model,
								  RedirectAttributes ra) {
		if(member == null) {return "redirect:/member/login";}

		// 단건 조회
		AddressBookDTO addr = addressBookService.getOneForOwner(addressNo, member.getMemberId());
		if(addr == null) {
			ra.addFlashAttribute("error", "존재하지 않거나 권한이 없는 주소입니다.");
			return "redirect:/member/address";
		}
			
		model.addAttribute("addr", addr);
		return "member/address-edit";
	}
	
	// 배송지 수정 저장
	@PostMapping("address/edit")
	public String editAddress(@SessionAttribute(name="user", required=false) MemberDTO member,
							  @RequestParam("addressNo") int addressNo,
							  @RequestParam("receiverName") String receiverName,
							  @RequestParam("receiverPhone") String receiverPhone,
							  @RequestParam("addressDetail") String addressDetail,
							  @RequestParam(value = "isDefault", required = false) String isDefault,
							  RedirectAttributes ra) {
		if(member == null) {
			return "redirect:/member/login";
			
		}
		
		// 검증 + 업데이트
		addressBookService.updateAddress(addressNo, member.getMemberId(), receiverName, receiverPhone, addressDetail);
		
		// 기본 배송지 체크가 Y면 그 주소를 기본으로 세팅
		if("Y".equals(isDefault)) {
			addressBookService.setDefaultAddress(addressNo, member.getMemberId());
		}
		
		ra.addFlashAttribute("msg", "배송지가 수정되었습니다.");
		return "redirect:/member/address";
	}
	
	
	//작업자 윤예솔
	//주문페이지에서 배송정보 저장
	@PostMapping("save-from-order")
	@ResponseBody
	public Map<String, Object> ajaxSaveFromOrder(HttpSession session, @RequestParam("changeName") String receiverName, 
							@RequestParam("changePhone") String receiverPhone, @RequestParam("changeAddress") String addressDetail){
		//결과를 리턴할 map객체
		Map<String, Object> response = new HashMap<String, Object>();

		//로그인 체크
		MemberDTO member = (MemberDTO)session.getAttribute("user");
		if(member==null) {
			response.put("status", "login");
			response.put("url", "/member/login");
			return response;
		}
		
		//주소지 추가
		try {
			addressBookService.addSubAddress(member, receiverName, receiverPhone, addressDetail);
			//주소지 추가에 성공했다면
			response.put("status", "success");
			return response;
		}catch(IllegalStateException e) {
			// 5개 초과 시 안내 메시지
			response.put("status", "error");
			return response;
		}		
	}

}