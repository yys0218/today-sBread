package com.ex.rider.model.data;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RiderInsertForm {

    @NotEmpty(message = "아이디는 필수 입력 항목입니다.")
    private String memberId;

    @NotEmpty(message = "비밀번호는 필수 입력 항목입니다.")
    private String memberPw;

    // 비밀번호 확인
    @NotEmpty(message = "비밀번호 확인은 필수 입력 항목입니다.")
    private String memberPwRe;

    @NotEmpty(message = "이름은 필수 입력 항목입니다.")
    private String memberName;

    private String emailLocal;

    private String emailDomain;

    // @Email(message = "올바른 이메일 형식이 아닙니다.")
    // @NotEmpty(message = "이메일은 필수 입력 항목입니다.")
    // private String memberEmail;

    // 날짜 타입은 NotEmpty를 사용하지 못함
    // NotEmpty는 문자열 , 컬렉션 , 배열 처럼 "비어있다"는 개념이 있는 타입만 지원
    @NotNull(message = "생일은 필수 입력 항목입니다.")
    @Past(message = "과거 날짜만 입력이 가능합니다.")
    private LocalDate memberBirth;

    private String roadAddress;

    @NotEmpty(message = "주소는 필수 입력 항목입니다.")
    private String detailAddress;

    @NotEmpty(message = "전화번호는 필수 입력 항목입니다.")
    private String memberPhone;

    private String memberGender;

    /*
     * Validation 라이브러리
     * 
     * @Size : 문자의 길이제한. (max , min)
     * 
     * @NotNull : Null 허용 X
     * 
     * @NotEmpty : 공백 , Null 허용 X
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
