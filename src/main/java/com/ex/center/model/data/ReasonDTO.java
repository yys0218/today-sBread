package com.ex.center.model.data;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

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
@Table(name = "reason")
public class ReasonDTO {
    // CREATE SEQUENCE reason_seq START WITH 1 INCREMENT BY 1;

    /** ID번호 */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reason_seq_gen")
    @SequenceGenerator(name = "reason_seq_gen", sequenceName = "reason_seq", allocationSize = 1)
    private int reasonNo;

    // /** 테이블 종류(0 : 신고, 1 : 거절, 2 : 폐점, 3 : 제재) */
    private int reasonTable;

    // // 신고 유형(각각 순서대로 0부터)
    private int reasonType;

    // 1씩 늘어날때마다의 시간
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private LocalDateTime reasonAt;

}
/*
  ReasonHistory.java (Entity Code)

    1 import jakarta.persistence.*;
    2 import org.springframework.data.annotation.CreatedDate;
    3 import org.springframework.data.jpa.domain.support.AuditingEntityListener;
    4 
    5 import java.time.LocalDateTime;
    6 
    7 
    8   신고, 거절 등 특정 사유가 발생한 이력(Event)을 기록하기 위한 엔티티입니다.
    9   이 테이블의 데이터는 수정되거나 삭제되지 않는 것을 원칙으로 합니다. (Immutable)
   10  
   11 @Entity
   12 // JPA Auditing 기능을 활성화하여 생성 시간을 자동으로 기록합니다.
   13 @EntityListeners(AuditingEntityListener.class)
   14 // 실제 데이터베이스 테이블의 이름과 인덱스를 지정합니다.
   15 @Table(name = "reason_history", indexes = {
   16     // 가장 핵심적인 복합 인덱스입니다.
   17     // 특정 테이블과 타입에 대해, 특정 기간 동안의 데이터를 조회하고 COUNT하는 쿼리 성능을 극대화합니다.
   18     @Index(name = "idx_reason_history_main_query", columnList = "reasonTable, reasonType, createdAt"),  
   19 
   20     // 만약 '특정 사용자가 어떤 활동을 했는지'를 조회하는 기능이 필요하다면,
   21     // 아래와 같이 memberId에 대한 인덱스를 추가할 수 있습니다.
   22     // @Index(name = "idx_reason_history_member_id", columnList = "memberId")
   23 })
   24 public class ReasonHistory {
   25 
   26     
   27       각 이력의 고유 식별자 (Primary Key)
   28      
   29     @Id
   30     @GeneratedValue(strategy = GenerationType.IDENTITY)
   31     private Long id;
   32 
   33     
   34       이벤트가 발생한 도메인 또는 테이블 (e.g., "product_report", "member_block")
   35       검색의 핵심 조건이므로 null을 허용하지 않고, 인덱스의 일부가 됩니다.
   36      
   37     @Column(nullable = false, length = 50)
   38     private String reasonTable;
   39 
   40     
   41       이벤트의 상세 타입 (e.g., "spam", "abuse")
   42       검색의 핵심 조건이므로 null을 허용하지 않고, 인덱스의 일부가 됩니다.
   43      
   44     @Column(nullable = false, length = 50)
   45     private String reasonType;
   46 
   47     
   48       이벤트를 발생시킨 사용자의 ID.
   49       어떤 사용자가 주로 어떤 활동을 하는지 분석할 때 유용합니다.
   50       요구사항에 따라 추가하거나 제외할 수 있습니다.
   51      
   52     // @Column(nullable = false)
   53     // private Long memberId;
   54 
   55     
   56       이벤트가 발생한 시간.
   57       JPA Auditing에 의해 자동으로 생성되며, 한 번 생성되면 절대 수정되지 않도록 설정합니다. (updatable = false)
   58       시간 기반 분석의 핵심 필드입니다.
   59      
   60     @CreatedDate
   61     @Column(nullable = false, updatable = false)
   62     private LocalDateTime createdAt;
   63 
   64     
   65       JPA는 기본 생성자를 필요로 합니다.
   66       접근 제어자를 protected로 설정하여, 외부에서 불필요한 객체 생성을 막습니다.
   67      
   68     protected ReasonHistory() {}
   69 
   70     
   71       서비스 레이어에서 객체를 생성할 때 편의성을 제공하기 위한 생성자입니다.
   72       @param reasonTable 이벤트 발생 도메인
   73       @param reasonType 이벤트 상세 타입
   74      
   75     public ReasonHistory(String reasonTable, String reasonType) {
   76         this.reasonTable = reasonTable;
   77         this.reasonType = reasonType;
   78     }
   79 
   80     // Getter 메서드들
   81     public Long getId() {
   82         return id;
   83     }
   84 
   85     public String getReasonTable() {
   86         return reasonTable;
   87     }
   88 
   89     public String getReasonType() {
   90         return reasonType;
   91     }
   92 
   93     public LocalDateTime getCreatedAt() {
   94         return createdAt;
   95     }
   96 }
   */