package com.ex.center.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ex.center.model.data.InquiryImg;

public interface InquiryImgRepository extends JpaRepository<InquiryImg, Integer> {

    // 해당 문의의 이미지 목록(노출 순서용)
    List<InquiryImg> findByInquiry_InquiryNoOrderByFileOrderAsc(int inquiryNo);

}
