package com.ex.center.model.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="faq") 
public class FaqDTO {
    //CREATE SEQUENCE faq_seq START WITH 1 INCREMENT BY 1;

    // ID 번호
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "faq_seq_gen")
	@SequenceGenerator(name = "faq_seq_gen", sequenceName = "faq_seq", allocationSize = 1)   
    private int faqNo;

    /** 카테고리(회원, 주문, 결제, 배송, 취소/환불, 쿠폰/포인트, 매장) */
    private String faqCategory;

    // 제목
    private String faqTitle;

    // 내용
    private String faqContent;

    /** 숨김유무(0 : 보임 / 1: 숨김) */
    private int faqStatus;

    // 조횟수
    private int readCount;
}