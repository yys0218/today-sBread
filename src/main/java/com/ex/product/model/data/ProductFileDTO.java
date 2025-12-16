package com.ex.product.model.data;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProductFileDTO { // 상품 이미지(파일)
	private int fileNo; // 파일번호
	private int ProductNo; // 상품번호
	private String fileName; // 파일명
	private String filePath; // 파일 경로
	private LocalDateTime fileReg; // 파일 업로드 일시
}
