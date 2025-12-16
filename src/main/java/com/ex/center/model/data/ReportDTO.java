package com.ex.center.model.data;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.ex.member.model.data.MemberDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="report") 
public class ReportDTO {
    //CREATE SEQUENCE report_seq START WITH 1 INCREMENT BY 1;

    // ID 번호
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq_gen")
	@SequenceGenerator(name = "report_seq_gen", sequenceName = "report_seq", allocationSize = 1)
    private int reportNo;

    // 작성자
    private int memberNo;
    
    /** 유형(0:회원 관련 신고, 1:매장 관련 신고, 2: 기타) */
    private Integer reportType;

    /** 사유(
     * 0 : 별점 테러(악성 리뷰)
     * 1 : 허위주문, 노쇼(허위 주문)
     * 2 : 악성 행위(욕설 비방)
     * 3 : 부정 이용(다계정 이용)
     * 4 : 위생 문제(유통기한 도과, 위생상태 불량)
     * 5 : 상품 문제(불량 제품)
     * 6 : 배송 문제(배달 지연, 누락)
     * 7 : 불친절(고객 응대 불만)
     * 8: 기타 
     * )*/
    private Integer reportReason;
    
    // 기타 시 입력
    private String reportEtc;

    // 대상(memberNick or shopname)
    private String reportRef;

    // 제목
    private String reportTitle;

    // 내용
    private String reportContent;

    // // 답변받을 메일
    // @Email
    // private String reportMail;

    // 작성날짜
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private LocalDateTime reportAt;

    // 답변
    private String reportReply;

    // 답변날짜
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime reportReplyAt;

    /** 상태(0 : 답변전, 1: 답변후, 2: 답변확인) */
    private Integer reportStatus;

    @OneToMany(mappedBy="report", cascade=CascadeType.REMOVE)
    private List<ReportImg> reportImg;

    @ManyToOne
    @JoinColumn(name = "memberNo", insertable = false, updatable = false)
    private MemberDTO member;
}
