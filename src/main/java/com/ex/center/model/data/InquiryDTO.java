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
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="inquiry") 
public class InquiryDTO {
    //CREATE SEQUENCE inquiry_seq START WITH 1 INCREMENT BY 1;

    // ID 번호
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inquiry_seq_gen")
	@SequenceGenerator(name = "inquiry_seq_gen", sequenceName = "inquiry_seq", allocationSize = 1)
    private int inquiryNo;

    // 작성자
    private int memberNo;

    /** 문의유형 코드 (0: 회원, 1: 매장, 2: 주문/배송, 3: 결제, 4: 기타) */
    private Integer inquiryType;
    
    // 제목
    private String inquiryTitle;

    // 내용
    private String inquiryContent;

    // 답변받을 이메일
    // @Email
    // private String inquiryMail;

    // 작성날짜
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private LocalDateTime inquiryAt;

    // 답변
    private String inquiryReply;

    // 답변 작성날짜
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime inquiryReplyAt;

    /** 상태(0: 미답변, 1:답변, 2:답변확인) */
    private int inquiryStatus;

        @OneToMany(mappedBy="inquiry", cascade=CascadeType.ALL)
    private List<InquiryImg> inquiryImg;

    @ManyToOne
    @JoinColumn(name = "memberNo", insertable = false, updatable = false)
    private MemberDTO member;
}
