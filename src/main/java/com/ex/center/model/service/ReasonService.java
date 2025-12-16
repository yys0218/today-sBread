// package com.ex.center.model.service;

// import java.util.Optional;

// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Service;

// import com.ex.center.model.data.ReasonDTO;
// import com.ex.center.model.repository.ReasonRepository;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class ReasonService {
    
//     private final ReasonRepository reasonRepository;

//     /**
//      * 사유 1 늘어나는 곳 마다 추가
//      * @Param table 0 : 신고, 1 : 거절, 2 : 폐점, 3 : 제재
//      * @Param type 각 테이블에 맞게
//      */
//     public void recordReason(@Param("reasonTable") int reasonTable, @Param("reasonType") int reasonType){
//         ReasonDTO reason = new ReasonDTO();
//         reason.setReasonTable(reasonTable);
//         reason.setReasonType(reasonType);
//         this.reasonRepository.save(reason);
//     }
// }
