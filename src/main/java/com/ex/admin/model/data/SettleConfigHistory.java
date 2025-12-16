package com.ex.admin.model.data;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class SettleConfigHistory {
    //CREATE SEQUENCE settle_config_history_seq START WITH 1 INCREMENT BY 1;

    // ID 번호
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "settle_config_history_seq_gen")
    @SequenceGenerator(name = "settle_config_history_seq_gen", sequenceName = "settle_config_history_seq", allocationSize = 1)
    private int historyId;

    // 빵집 수수료(백분율)
    private int shopRatio;

    // 배달 수수료(백분율)
    private int riderRatio;

    // 수정날짜
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
}
