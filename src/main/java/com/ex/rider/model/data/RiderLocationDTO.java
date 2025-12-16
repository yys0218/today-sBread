package com.ex.rider.model.data;

import java.time.LocalDateTime;

import com.ex.member.model.data.MemberDTO;
import com.ex.order.model.data.OrderDetailDTO;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "RiderLocation")
public class RiderLocationDTO {

    //
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rider_location_seq")
    @SequenceGenerator(name = "rider_location_seq", sequenceName = "rider_location_seq", allocationSize = 1)
    private long locationNo;

    // RiderLocation N : OrderDetail 1
    // 한개의 주문은 여러개의 RiderLocation을 가질 수 있다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_no", nullable = false) // FK
    private OrderDetailDTO order;

    // RiderLocation N : Member 1 (라이더)
    // 라이더는 여러개의 RiderLocation을 남길수 있다
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false) // FK
    private MemberDTO member;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
