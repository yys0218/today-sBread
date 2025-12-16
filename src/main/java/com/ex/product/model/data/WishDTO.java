package com.ex.product.model.data;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WishDTO {
	private int wishNo;					// 찜 고유 번호
	private int memberNo;				// 회원 고유 번호
	private int productNo;				// 상품 고유 번호
	private LocalDateTime createDate;	// 생성일
}