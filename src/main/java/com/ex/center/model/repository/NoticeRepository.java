package com.ex.center.model.repository;

import java.util.List;

import com.ex.center.model.data.NoticeDTO;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<NoticeDTO, Integer> {

    @Query("select n from NoticeDTO n where ( :kw is null or n.noticeTitle like %:kw% or n.noticeContent like %:kw% ) and ( :category is null or n.noticeCategory = :category )")
    Page<NoticeDTO> listNotice(@Param("kw") String kw, @Param("category") String category, Pageable pageable);

    @Query("select n from NoticeDTO n order by n.noticeNo desc fetch first 3 rows only")
    List<NoticeDTO> noticeMainList();

    @Query("select n from NoticeDTO n where (n.noticeTitle like %:kw% or n.noticeContent like %:kw%) and n.noticeStatus = 0 order by n.noticeNo desc fetch first 5 rows only")
    List<NoticeDTO> search(@Param("kw") String kw);
}
