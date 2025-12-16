package com.ex.alert.model.data;

import java.time.LocalDateTime;

import com.ex.member.model.data.MemberDTO;
import com.ex.order.model.data.OrderHistoryDTO;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "alert_history")
/**
 * 알림 이력(History)을 저장/전달하는 DTO 클래스
 *
 * <p>
 * 특정 주문(OrderHistory)와 연계된 알림 정보를 담아두는 객체입니다.
 * </p>
 *
 * <ul>
 * <li>알림 번호(alertNo) : 알림 식별자 (PK 역할)</li>
 * <li>알림 유형(alertType) : 알림 구분 handler명(예: 주문, 배송, 시스템 등)</li>
 * <li>관련 주문(order) : 해당 알림과 연계된 주문 정보</li>
 * <li>발신자(alertForm) : 알림을 발생시킨 회원 번호</li>
 * <li>수신자(alertTo) : 알림을 받은 회원 번호</li>
 * <li>읽음 여부(isRead) : 알림 확인 상태 (0:읽지않음/1:읽음)</li>
 * <li>상태(status) : 알림 상태 코드 (0:대기/1:삭제)</li>
 * <li>생성일시(createdAt) : 알림 생성 시각</li>
 * </ul>
 */
public class AlertHistoryDTO {

    // 알림 고유 번호
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alert_history_seq")
    @SequenceGenerator(name = "alert_history_seq", sequenceName = "alert_history_seq", allocationSize = 1)
    private long alertNo;

    // 알림 유형
    private String alertType;

    // 관련 주문
    @ManyToOne
    private OrderHistoryDTO order;

    // 발신자 (보내는사람)
    @ManyToOne
    private MemberDTO alertForm;

    // 수신자 (받는사람)
    @ManyToOne
    private MemberDTO alertTo;

    // 읽음 여부
    private int isRead;

    // 알림 상태
    private int status;

    // 생성 시간
    private LocalDateTime createdAt;

}
