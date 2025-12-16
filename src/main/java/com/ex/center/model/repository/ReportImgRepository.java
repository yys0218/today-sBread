package com.ex.center.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ex.center.model.data.ReportImg;

public interface ReportImgRepository extends JpaRepository<ReportImg, Integer> {

    // 해당 문의의 이미지 목록(노출 순서용)
    List<ReportImg> findByReport_ReportNoOrderByFileOrderAsc(int reportNo);

}
