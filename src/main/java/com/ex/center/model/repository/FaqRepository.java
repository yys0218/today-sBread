package com.ex.center.model.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.ex.center.model.data.FaqDTO;

public interface FaqRepository extends JpaRepository<FaqDTO, Integer> {

    // 메인: 선택 카테고리 & 조회수 상위 5
    @Query("select f from FaqDTO f where (:category is null or f.faqCategory = :category) and f.faqStatus=0 order by f.readCount desc fetch first 5 rows only")
    List<FaqDTO> faqMainList(@Param("category") String category);

    // FAQ 게시판: 전체/카테고리 + 검색
    @Query("select n from FaqDTO n where ( :kw is null or n.faqTitle like %:kw% or n.faqContent like %:kw% ) and ( :category is null or n.faqCategory = :category )")
    Page<FaqDTO> listFaq( @Param("kw") String kw, @Param("category") String category, Pageable pageable );

    @Query("select f from FaqDTO f where (f.faqTitle like %:kw% or f.faqContent like %:kw%) and f.faqStatus = 0 order by f.faqNo desc fetch first 5 rows only")
    List<FaqDTO> search(@Param("kw") String kw);

    // 조회수 증가
    @Modifying(clearAutomatically = true)
    @Transactional   
    @Query("update FaqDTO f set f.readCount = f.readCount + 1 where f.faqNo = :faqNo")
    void increaseReadCount(@Param("faqNo") int faqNo);

}
