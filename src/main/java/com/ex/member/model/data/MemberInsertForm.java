package com.ex.member.model.data;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberInsertForm {

	@NotEmpty(message = "아이디는 필수 입력 항목입니다.")
	private String memberId;
	
	@NotEmpty(message = "비밀번호는 필수 입력 항목입니다.")
	private String memberPw;
	
	// 비밀번호 확인
	@NotEmpty(message = "비밀번호 확인은 필수 입력 항목입니다.")
	private String memberPw1;
	
	@NotEmpty(message = "이름은 필수 입력 항목입니다.")
	private String memberName;
	
	@NotEmpty(message = "닉네임은 필수 입력 항목입니다.")
	private String memberNick;
	
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@NotEmpty(message = "이메일은 필수 입력 항목입니다.")
	private String memberEmail;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate memberBirth;
	
	@NotEmpty(message = "주소는 필수 입력 항목입니다.")
	private String memberAddress;
	
	@NotEmpty(message = "전화번호는 필수 입력 항목입니다.")
	private String memberPhone;
	
	private String memberGender;

	@NotEmpty(message = "이메일 앞부분은 필수 입력 항목입니다.")
	private String emailLocal;

	@NotEmpty(message = "이메일 도메인은 필수 입력 항목입니다.")
	private String emailDomain;
}
