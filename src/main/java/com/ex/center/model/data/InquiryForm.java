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
public class InquiryForm {
    
    /** 문의유형 코드 (0: 회원, 1: 매장, 2: 주문/배송, 3: 결제, 4: 기타) */
    @NotNull(message="문의유형을 설정해주세요.")
    private Integer inquiryType;

    @NotNull(message="제목을 기입해주세요.")
    private String inquiryTitle;

    @NotNull(message="문의내용을 기입해주세요.")
    private String inquiryContent;

    // @Email
    // @NotNull(message="답변 받을 이메일을 기입해주세요.")
    // private String inquiryMail;

    private List<MultipartFile> images;

    private String inquiryReply;
}
