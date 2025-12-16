package com.ex.member.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.AddressBookDTO;

// AddressBook(배송지) 엔티티의 DB 접근 계층
// 기본적인 CRUD 메서드 (save, findById, findAll, deleteById 등)를 제공 받는다.
@Repository
public interface AddressBookRepository extends JpaRepository<AddressBookDTO, Integer> {
	
	// 특정 회원 (memberId)이 배송지를 하나라도 가지고 있는지 (true/false)
	// 컨트롤러/서비스에서 "최초 생성" 분기할 때 사용 가능
	boolean existsByMember_MemberId(String memberId);
	
	// 특정 회원의 배송지 총 개수 (5개로 제한)
	// "최대 5개 제한" 같은 검증에 사용
	long countByMember_MemberId(String memberId);
	
	
	// Pageable을 이용해 "정렬 + 상위 1건"을 가져오는 패턴
	// - 정렬 : 기본(Y)먼저, 그 다음 addressNo 오름차순
	// ex) repo.findDefaultOne(memberId, PageRequest.of(0,1)).stream().findFirst().orElse(null);
	@Query("select a from AddressBookDTO a where a.member.memberId = :memberId order by case when a.isDefault = 'Y' then 1 else 0 end desc, a.addressNo asc")
	List<AddressBookDTO> findDefaultOne(@Param("memberId") String memberId, Pageable pageable);
	
	// 회원의 모든 배송지 조회
	// addressNo 오름차순으로 정렬
	@Query("select a from AddressBookDTO a where a.member.memberId = :memberId order by a.addressNo asc")
	List<AddressBookDTO> findAllByMemberId(@Param("memberId") String memberId);

	@Query("select a from AddressBookDTO a where a.member.memberId = :memberId order by case when a.isDefault = 'Y' then 1 else 0 end desc, a.addressNo asc")
	List<AddressBookDTO> findAllSorted(@Param("memberId") String memberId);

	// 기본 배송지 1건 간단 조회
	Optional<AddressBookDTO> findFirstByMember_MemberIdAndIsDefault(String memberId, String isDefault);
	
	// 단건 조회
	Optional<AddressBookDTO> findByAddressNoAndMember_MemberId(int addressNo, String memberId);
	
	// 소유권 검증 존재 여부
	boolean existsByAddressNoAndMember_MemberId(int addressNo, String memberId);
	
	// 기본 배송지 일괄 해제
	@Modifying(clearAutomatically = true, flushAutomatically =true)
	@Query("update AddressBookDTO a set a.isDefault = 'N' where a.member.memberId = :memberId and a.isDefault = 'Y'")
	int clearDefault(@Param("memberId") String memberId);
	
	// 특정 주소지를 기본 배송지로 설정
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update AddressBookDTO a set a.isDefault = 'Y' where a.addressNo = :addressNo and a.member.memberId = :memberId")
	int setDefault(@Param("addressNo") int addressNo, @Param("memberId") String memberId);
}