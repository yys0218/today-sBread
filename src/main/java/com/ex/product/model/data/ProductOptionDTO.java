package com.ex.product.model.data;

import lombok.Data;

@Data
public class ProductOptionDTO { //상품 옵션 DTO (MyBatis)
	
	private int optionNo;	//옵션 번호
	
	private int productNo;	//상품 번호
	
	private String optionName;	//옵션 이름
	
	private int optionPrice;	//옵션 가격
}
