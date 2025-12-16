package com.ex.center.model.data;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeForm {
    
    @NotEmpty(message="카테고리를 설정해주세요.")
    private String category;

    @NotEmpty(message="제목이 비어있습니다.")
    private String title;

    @NotEmpty(message="본문이 비어있습니다.")
    private String content;
}
