package com.ex.center.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ex.center.model.data.ReportDTO;

public interface ReportRepository extends JpaRepository<ReportDTO, Integer> {

    @Query(" select r from ReportDTO r where r.memberNo = :memberNo and ( :all = true or r.reportStatus in :statusList ) order by r.reportAt desc")
    Page<ReportDTO> reportList(@Param("memberNo") int memberNo, @Param("all") boolean all,
            @Param("statusList") List<Integer> statusList, Pageable pageable);

    // status 와 type 둘 다 조건에 맞는 경우
    Page<ReportDTO> findByReportStatusAndReportType(int reportStatus, int reportType, Pageable pageable);

    // status 만 필터링
    Page<ReportDTO> findByReportStatus(int reportStatus, Pageable pageable);

    // type 만 필터링
    Page<ReportDTO> findByReportType(int reportType, Pageable pageable);

    Page<ReportDTO> findByReportStatusIn(List<Integer> statuses, Pageable pageable);

    Page<ReportDTO> findByReportStatusInAndReportType(List<Integer> statuses, int type, Pageable pageable);

    int countByreportStatusAndMemberNo(int reportStatus, int memberNo); // 메인 배지용(=2)

    @Query("SELECT r FROM ReportDTO r " +
            "WHERE (:reportType = -1 OR r.reportType = :reportType) " +
            "AND (:reportStatus = -1 OR " +
            "     (:reportStatus = 0 AND r.reportStatus = 0) OR " +
            "     (:reportStatus = 1 AND (r.reportStatus = 1 OR r.reportStatus = 2)))")
    Page<ReportDTO> searchReportList(@Param("reportType") int reportType,
            @Param("reportStatus") int reportStatus,
            Pageable pageable);
}
