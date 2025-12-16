package com.ex.product.model.data;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.validator.constraints.Range;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProductInsertForm { //상품 등록 시 insertForm의 유효성 검사 객체로 사용됨
	
	@NotNull
	private int productNo;				//상품번호
	
	@NotEmpty(message="상품명을 입력해주세요.")
	@Size(max=30)
	private String productName;			//상품명
	
	@NotNull(message="가격을 입력해주세요.")//int에는 @NotNull
	@Range(min=0, max=100000000, message="입력 가능 가격은 0원에서 1,00,000,000원까지 입니다.")
	private int price;			//상품가격
	
	private Integer isSubscription;		//정기 결제 여부
	
	@NotNull
	private int memberNo;				//판매자 멤버 번호
	
	@NotNull
	private int shopNo;					//상점 번호
	
	@Range(min=1, message="상품사진은 1개 이상 업로드해주세요.")
	private int countFiles;		//업로드된 파일 수
		
	@NotEmpty(message="카테고리를 선택해주세요.")
	private String categoryMain;		//카테고리 대분류
	
	private String categorySub;			//카테고리 소분류
	
	@NotEmpty(message="상세 정보를 입력해주세요.")
	private String productInfo;			//상세 정보
	
	private String productSummary;		//상품 한 줄 소개
	
	@NotEmpty(message="알레르기 정보를 입력해주세요.")
	private String allergyInfo;			//알레르기 정보
	
	@NotEmpty(message="영양성분을 입력해주세요.")
	private String nutritionInfo;		//영양성분
	
	private List<ProductOptionForm> options;		//상품 옵션
	
	private int status;					//상품 상태
	
	private LocalDateTime createAt;		//등록 날짜

	private LocalDateTime updateAt;		//수정 날짜
	
    /*
     * Validation 라이브러리
     * 
     * @Size : 문자의 길이제한. (max , min)
     * 
     * @NotNull : Null 허용 X
     * 
     * @NotEmpty : 공백 , Null 허용 X String일 때
     * 
     * @Past : 과거 날짜만 입력 가능
     * 
     * @Future : 미래 날짜만 입력 가능
     * 
     * @FutureOrPresent : 미래 또는 오늘 날짜만 가능
     * 
     * @Max : 최대값 이하의 숫자만 입력 가능
     * 
     * @Min : 최소값 이하의 숫자만 입력 가능
     * 
     * @Pattern : 입력값을 정규식 패턴으로 검증
     */
	
}
