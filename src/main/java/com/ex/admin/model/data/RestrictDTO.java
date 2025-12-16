package com.ex.admin.model.data;

import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "restrict")
public class RestrictDTO {
    // CREATE SEQUENCE restrict_seq START WITH 1 INCREMENT BY 1;

    /** ID 번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "restrict_seq_gen")
    @SequenceGenerator(name = "restrict_seq_gen", sequenceName = "restrict_seq", allocationSize = 1)
    private int restrictNo;

    /** 대상 번호(memberNo) */
    private int memberNo;

    /** 대상 기존 롤 값 */
    private int memberRole;

    // 사유
    /**
     * 회원 제재 사유 : 
     * 0 : 운영방침 위반(불법 상품 판매, 허위 정보 게시, 타인 명의 도용 등 운영 정책 위반.)
     * 1 : 부적절한 이용행위(욕설, 비방, 음란물, 도배 행위 등 커뮤니티 질서를 해치는 행위.)
     * 2 : 결제관련 문제(반복적인 미결제, 결제 취소, 환불 악용 등 거래 안정성 저해.)
     * 3 : 법적 문제(저작권 침해, 개인정보 유출, 법령 위반 등으로 인해 법적 분쟁 소지가 있는 경우.)
     * 4 : 판매자 피해 누적()
     * 
     * 빵집 제재 사유 : 
     * 5 : 위생 및 품질 문제(반복적인 불량, 유통기한 도과 제품)
     * 6 : 배송,운영 불성실(주문 누락, 과도한배송 지연)
     * 7 : 허위,과장 광고(실제 판매하지 않는 메뉴, 이미지와 현저히 다른 상품)
     * 8 : 정산, 계약 위반
     * 9 : 소비자 피해 누적(다수의 고객불만)
     */
    private int restrictReason;
    
    /** 제재 유형(3일, 1주일, 1달, 영정) */
    private String restrictType;

    // 제재 날짜
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime restrictAt;

    // 제재 기간
    @Temporal(TemporalType.DATE)
    private LocalDate restrictPeriod;

}
