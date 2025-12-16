package com.ex.center.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ex.center.model.data.InquiryDTO;

public interface InquiryRepository extends JpaRepository<InquiryDTO, Integer> {

    @Query(" select i from InquiryDTO i where i.memberNo = :memberNo and ( :all = true or i.inquiryStatus in :statusList ) order by i.inquiryAt desc")
    Page<InquiryDTO> inquiryList(@Param("memberNo") int memberNo, @Param("all") boolean all,
            @Param("statusList") List<Integer> statusList, Pageable pageable);

    // status 와 type 둘 다 조건에 맞는 경우
    Page<InquiryDTO> findByInquiryStatusAndInquiryType(int inquiryStatus, int inquiryType, Pageable pageable);

    // status 만 필터링
    Page<InquiryDTO> findByInquiryStatus(int inquiryStatus, Pageable pageable);

    // type 만 필터링
    Page<InquiryDTO> findByInquiryType(int inquiryType, Pageable pageable);

    Page<InquiryDTO> findByInquiryStatusIn(List<Integer> statuses, Pageable pageable);

    Page<InquiryDTO> findByInquiryStatusInAndInquiryType(List<Integer> statuses, int type, Pageable pageable);

    int countByInquiryStatusAndMemberNo(int inquiryStatus, int memberNo); // 메인 배지용(=2)

    @Query("SELECT i FROM InquiryDTO i " +
            "WHERE (:inquiryType = -1 OR i.inquiryType = :inquiryType) " +
            "AND (:inquiryStatus = -1 OR " +
            "     (:inquiryStatus = 0 AND i.inquiryStatus = 0) OR " +
            "     (:inquiryStatus = 1 AND (i.inquiryStatus = 1 OR i.inquiryStatus = 2)))")
    Page<InquiryDTO> searchInquiryList(@Param("inquiryType") int inquiryType,
            @Param("inquiryStatus") int inquiryStatus,
            Pageable pageable);
}
