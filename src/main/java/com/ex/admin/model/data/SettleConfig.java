package com.ex.admin.model.data;

import java.time.LocalDateTime;

import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class SettleConfig {

    // 판매자 정산 수수료(백분율)
    private int shopRatio;

    // // 라이더 정산 수수료(백분율)
    private int riderRatio;

    // // 수정한 날짜
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
}
