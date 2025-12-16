package com.ex.product.model.data;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductOptionForm { //상품 옵션 유효성 검사
	
	@NotEmpty(message="옵션 이름을 입력해주세요.")
	private String name;	//옵션 이름
	
	@Range(min=0, max=100000000, message="입력 가능 가격은 0원에서 1,00,000,000원까지 입니다.")
	private Integer price; //옵션 가격
}
