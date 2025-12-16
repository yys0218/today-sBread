package com.ex.admin.model.data;

import java.time.LocalDateTime;

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
@Table(name="settle") 
public class SettleDTO {
    //CREATE SEQUENCE settle_seq START WITH 1 INCREMENT BY 1;

    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "settle_seq_gen")
	@SequenceGenerator(name = "settle_seq_gen", sequenceName = "settle_seq", allocationSize = 1)
    private int settleNo;

    /** 대상 MemberRole 1 : 빵집 , 4 : 라이더*/
    private int settleType;

    /** 대상 memberNo */
    private int settleRef;

    /** 대상 이름 memberNick, shopName */
    private String settleName;

    // 금액
    private int settleAmt;

    // 수수료
    private int settleCharge;

    // 작성날짜
    private LocalDateTime settleAt; 
}
