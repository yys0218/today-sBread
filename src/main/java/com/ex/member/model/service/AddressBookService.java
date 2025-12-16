package com.ex.member.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ex.member.model.data.AddressBookDTO;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.AddressBookRepository;
import com.ex.rider.model.service.KakaoLocalService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressBookService {
	private final AddressBookRepository addressRepository;
	private final KakaoLocalService kakaoLocalService;

	// 회원가입 직후 1회 호출
	// 해당 회원이 아직 배송지가 없다면, 가입 시 입력한 주소 정보를 기본 배송지로 1건 생성
	public void createDefaultAddressFromSignup(MemberDTO member) {
		String memberId = member.getMemberId();
		
		// 이미 배송지가 있으면 아무 것도 안함
		if(addressRepository.existsByMember_MemberId(memberId)) return;
		
		AddressBookDTO a = new AddressBookDTO();
		a.setMember(member);
		
		// memberDTO
		a.setReceiverName(member.getMemberName()); 			// 수령인 이름 (회원가입 시 회원 이름)
		a.setReceiverPhone(member.getMemberPhone());		// 수령인 연락처
		a.setAddressDetail(member.getMemberAddress());		// 수령인 주소
		a.setIsDefault("Y"); 								// 기본 배송지
		a.setCreatedAt(LocalDateTime.now()); 				// createdAt 직접 세팅 
		
		// 좌표 변환 (선택)
//		setGeoIfPossible(a);
		
		addressRepository.save(a);
	}
	
	// 마이페이지에서 보여줄 "기본 배송지" 가져오기
	// 기본(Y) 우선 정렬 + addressNo asc 로 1건만 가져옴
	public AddressBookDTO getMyOnlyAddress(MemberDTO member) {
		List<AddressBookDTO> list = addressRepository.findDefaultOne(member.getMemberId(), PageRequest.of(0, 1));
		return list.isEmpty() ? null : list.get(0);
	}

	// 배송지 추가
	// 추가되는 주소는 기본값이 아님(N)으로 저장 (기본은 1개 유지)
	public void addSubAddress(MemberDTO member, String receiverName, String receiverPhone, String addressDetail) {
		
		// 현재 배송지 개수 확인
		long count = addressRepository.countByMember_MemberId(member.getMemberId());
		
		if(count >= 5) {
			// 5개 이상이면 예외 던져서 컨트롤러에서 메시지 처리
			throw new IllegalStateException("배송지는 최대 5개까지 등록할 수 있습니다.");
		}
		
		AddressBookDTO a = new AddressBookDTO();
		a.setMember(member);
		a.setReceiverName(receiverName);
		a.setReceiverPhone(receiverPhone);
		a.setAddressDetail(addressDetail);
		a.setIsDefault("N"); 	 		// 추가 주소는 기본이 아님
		a.setCreatedAt(LocalDateTime.now()); 	// 생성일
		
		// 좌표 변환 (선택)
//		setGeoIfPossible(a);
		
		addressRepository.save(a);
	}
	
	// 기본 배송지 설정
	// 해당 회원의 모든 주소 기본값을 N으로 초기화함
	// 선택한 addressNo만 Y로 설정함
	public void setDefaultAddress(int addressNo, String memberId) {
		
		// 회원의 전체 주소 조회
		List<AddressBookDTO> all = addressRepository.findAllByMemberId(memberId);
		
		// 모두 N으로 변경
		for(AddressBookDTO addr : all) {
			addr.setIsDefault("N");
		}
		
		// 라디오 버튼으로 선택한 주소만 Y로 변경
		AddressBookDTO selected = all.stream()
				.filter(a -> a.getAddressNo() == addressNo)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("해당 배송지를 찾을 수 없습니다."));
		
		selected.setIsDefault("Y");
	}
	
	// 전체 주소 리스트
	public List<AddressBookDTO> getAllAddresses(String memberId){
		return addressRepository.findAllSorted(memberId);
	}
	
	// 내가 소유한 특정 배송지 1건 조회
	public AddressBookDTO getOneForOwner(int addressNo, String memberId) {
		return addressRepository.findByAddressNoAndMember_MemberId(addressNo, memberId)
				.orElse(null);
	}
	
	// 배송지 수정
	public void updateAddress(int addressNo, String memberId, String receiveName, String receivePhone, String addressDetail) {
		AddressBookDTO addr = addressRepository.findByAddressNoAndMember_MemberId(addressNo, memberId)
				.orElseThrow(() -> new IllegalArgumentException("해당 배송지를 찾을 수 없거나 권한이 없습니다."));
		
		addr.setReceiverName(receiveName);
		addr.setReceiverPhone(receivePhone);
		addr.setAddressDetail(addressDetail);
		
		// 좌표 반환
		// setGeoIfPossible(addr);
		
	}

	// 배송지 삭제
	public void deleteAddress(int addressNo, String memberId) {
	    // 1) 소유/존재 검증
	    AddressBookDTO target = addressRepository
	            .findByAddressNoAndMember_MemberId(addressNo, memberId)
	            .orElseThrow(() -> new IllegalArgumentException("해당 배송지를 찾을 수 없거나 권한이 없습니다."));

	    // 2) 최소 1건은 유지(마지막 1건이면 삭제 불가)
	    long count = addressRepository.countByMember_MemberId(memberId);
	    if (count <= 1) {
	        throw new IllegalStateException("배송지는 최소 1개는 유지해야 합니다.");
	    }

	    // 3) 기본 배송지를 삭제하는 경우를 대비해 플래그 보관
	    boolean wasDefault = "Y".equalsIgnoreCase(target.getIsDefault());

	    // 4) 삭제
	    addressRepository.delete(target);

	    // 5) 기본 배송지였으면, 남은 것 중 하나를 기본으로 지정 (가장 앞의 한 건)
	    if (wasDefault) {
	        List<AddressBookDTO> rest = addressRepository.findAllByMemberId(memberId);
	        if (!rest.isEmpty()) {
	            // 혹시 모를 중복 Y를 방지: 모두 N으로 초기화
	            for (AddressBookDTO a : rest) a.setIsDefault("N");
	            // 첫번째를 기본으로
	            rest.get(0).setIsDefault("Y");
	        }
	    }
		
	}
	
	// 좌표 변환 : 주소가 있으면 카카오 로컬 API로 위/경도 세팅
//	private void setGeoIfPossible(AddressBookDTO a) {
//		String addr = a.getAddressDetail();
//		if(addr != null && !addr.isBlank()) {
//			try {
//				Map<String, Double> map = kakaoLocalService.getCoordinates(addr); // {"X":lon, "Y":lat}
//				a.setLongitude(map.get("X"));
//				a.setLatitude(map.get("Y"));
//			}catch(Exception ignore) {
				// 좌표 반환 실패해도 저장은 진행함
//			}
//		}
//	}
}