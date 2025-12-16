package com.ex.rider.model.data;

import java.lang.reflect.Member;
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
@Table(name = "delivery_fee")
public class DeliveryFeeDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delivery_fee_seq")
    @SequenceGenerator(name = "delivery_fee_seq", sequenceName = "delivery_fee_seq", allocationSize = 1)
    private long feeNo;

    @ManyToOne
    private MemberDTO member;

    @ManyToOne
    private OrderHistoryDTO orderHistory;

    private int feeType;

    private int feeAmount;

    private int feeBalance;

    private LocalDateTime createdAt;
}
