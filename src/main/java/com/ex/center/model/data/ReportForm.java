package com.ex.center.model.data;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportForm {
    
    /** 유형(0:회원, 1:매장, 2:기타) */
    @NotNull(message="신고 유형을 선택해주세요.")
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
    @NotNull(message="신고사유를 설정해주세요.")
    private Integer reportReason;
    
    // 기타 시 입력
    private String reportEtc;

    
    @NotEmpty(message="제목을 기입해주세요.")
    private String reportTitle;

    @NotEmpty(message="신고대상을 기입해주세요.")
    private String reportRef;

    @NotEmpty(message="신고내용을 기입해주세요.")
    private String reportContent;

    // @Email
    // @NotEmpty(message="답변 받을 이메일을 기입해주세요.")
    // private String reportMail;

    private List<MultipartFile> images;    

    private String reportReply;
}
