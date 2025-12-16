package com.ex.admin.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ex.admin.model.data.RestrictDTO;

public interface RestrictRepository extends JpaRepository<RestrictDTO, Integer>{
    
    RestrictDTO findTopByMemberNoOrderByRestrictNoDesc(int memberNo);
    int countByMemberNo(int memberNo);
    int findMemberRoleByMemberNo(int memberNo);
    RestrictDTO findTopByMemberNo(int memberNo);
    RestrictDTO findTopByMemberNoOrderByRestrictAtDesc(int memberNo);

    // 차트
    @Query("SELECT r.restrictReason, COUNT(r) FROM RestrictDTO r GROUP BY r.restrictReason")
    List<Object[]> findRestrictReason();

    @Query("SELECT r.restrictType, COUNT(r) FROM RestrictDTO r GROUP BY r.restrictType")
    List<Object[]> findRestrictType();

    @Query("SELECT TO_CHAR(r.restrictAt,'YYYY-MM'), COUNT(r) " +
           "FROM RestrictDTO r " +
           "GROUP BY TO_CHAR(r.restrictAt,'YYYY-MM') " +
           "ORDER BY TO_CHAR(r.restrictAt,'YYYY-MM')")
    List<Object[]> findRestrictByMonth();
}
