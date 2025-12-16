package com.ex.center.model.data;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="notice") 
public class NoticeDTO {
    //CREATE SEQUENCE notice_seq START WITH 1 INCREMENT BY 1;

    // ID 번호
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notice_seq_gen")
	@SequenceGenerator(name = "notice_seq_gen", sequenceName = "notice_seq", allocationSize = 1)
    private int noticeNo;

    /** 카테고리(공지, 점검, 이벤트, 업데이트) */
    private String noticeCategory;

    // 제목
    private String noticeTitle;

    // 내용
    private String noticeContent;

    // 작성날짜
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private LocalDateTime noticeAt;
    
    // 수정날짜
    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private LocalDateTime noticeUpdateAt;

    /** 숨김처리(0 : 보임, 1 : 숨김) */
    private int noticeStatus;

}
