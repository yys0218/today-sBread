package com.ex.member.model.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.data.MemberInsertForm;
import com.ex.member.model.repository.MemberRepository;
import com.ex.rider.model.service.KakaoLocalService;

import lombok.RequiredArgsConstructor;

// MemberService
// "회원"과 관련된 핵심 비즈니스 로직을 담당하는 계층
// 컨트롤러가 요청을 받으면, 여기서 검증/암호화/중복체크/토큰발급/메일전송 등 도메인 로직을 수행
// 최종적으로 Repository를 통해 DB에 읽기/쓰기를 함.

// 필수 Bean
// MemberRepository : 회원 테이블 CRUD
// PasswordEncoder : 비밀번호 암호화 (BCrypt) -> 항상 암호화해서 저장
// JavaMailSender : 임시 비밀번호 메일로 발송
// KakaoLocalService : 주소 -> 좌표 변환
@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository; // DB 연동용 Repository
	private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 (BCrypt)
	private final JavaMailSender mailSender; // 이메일 전송용
	private final KakaoLocalService kakaoLocalService; // 카카오 로컬 서비스 ( 주소 -> 좌표 변환 )

	// [회원가입] register()
	// 컨트롤러 signup(POST) 에서 호출
	// 1. 비밀번호 일치 여부 확인
	// 2. 아이디, 이메일, 닉네임 중복 체크
	// 3. 비밀번호 암호화
	// 4. 주소로 좌표 조회
	// 5. DB에 저장
	// 실패 시 IllegalArgumentException 등 예외 던져서 컨트롤러에서 에러 메시지 처리
	public MemberDTO register(MemberInsertForm form) {
		// 1. 비밀번호와 확인 비밀번호가 같은지 검사
		if (!form.getMemberPw().equals(form.getMemberPw1())) {
			throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
		}
		// 2. 아이디/이메일/닉네임 중복 여부 체크
		if (memberRepository.findByMemberId(form.getMemberId()).isPresent()) {
			throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
		}
		if (memberRepository.findByMemberEmail(form.getMemberEmail()).isPresent()) {
			throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
		}
		if (memberRepository.findByMemberNick(form.getMemberNick()).isPresent()) {
			throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
		}
		if(memberRepository.findByMemberPhone(form.getMemberPhone()).isPresent()) {
			throw new IllegalArgumentException("이미 사용중인 전화번호입니다.");
		}

		// 3. DB에 저장한 엔티티 생성
		MemberDTO dto = new MemberDTO();
		dto.setMemberId(form.getMemberId());
		dto.setMemberPw(passwordEncoder.encode(form.getMemberPw().trim())); // 비밀번호 암호화
		dto.setMemberName(form.getMemberName());
		dto.setMemberNick(form.getMemberNick());
		dto.setMemberEmail(form.getMemberEmail());
		dto.setMemberBirth(form.getMemberBirth());
		dto.setMemberAddress(form.getMemberAddress());
		dto.setMemberPhone(form.getMemberPhone());
		dto.setMemberGender(form.getMemberGender());
		dto.setIsTemporary(0); // 임시비밀번호가 아닐때 기본값 : 0 / 임시비밀번호일때 : 1
		dto.setMemberRole(0); // 기본 회원 권한 0 : USER

		// 4. 주소 -> 좌표 변환
		try {
			Map<String, Double> map = this.kakaoLocalService.getCoordinates(form.getMemberAddress());
			if (map != null) {
				dto.setLongitude(map.get("longitude")); // 경도
				dto.setLatitude(map.get("latitude")); // 위도
			}
		} catch (Exception igonore) {

		}

		// 5. 저장 후, 저장된 엔티티 반환
		return memberRepository.save(dto);
	}

	public void dummyRegister() {

		for (int i = 1; i <= 100; i++) {
			MemberDTO dto = new MemberDTO();
			dto.setMemberId("test" + i);
			dto.setMemberPw(passwordEncoder.encode("1234")); // 비밀번호 암호화
			dto.setMemberName("회원" + i);
			dto.setMemberNick("memberNick" + i);
			dto.setMemberEmail("test" + i + "@example.com");
			// i를 이용해서 날짜 만들기 (1~28일)
			String dateStr = "1990-01-" + String.format("%02d", (i % 28) + 1);
			// String → LocalDate 변환
			dto.setMemberBirth(LocalDate.parse(dateStr));
			dto.setMemberPhone("0101234" + String.format("%04d", i));
			dto.setMemberGender(i % 2 == 0 ? "M" : "F");
			dto.setMemberRole(0); // 기본 회원 권한 0 : USER

			// 관악구 주소 자동 생성 (예: 관악로, 신림로, 봉천로 번갈아가며)
			String[] addresses = new String[] {
					"서울시 관악구 관악로 1", // 서울대학교 등 :contentReference[oaicite:1]{index=1}
					"서울시 관악구 관악로 163", // 효원빌딩 :contentReference[oaicite:2]{index=2}
					"서울시 관악구 관악로 273-14", // :contentReference[oaicite:3]{index=3}
					"서울시 관악구 관악로 273-18", // :contentReference[oaicite:4]{index=4}
					"서울시 관악구 관악로 263", // :contentReference[oaicite:5]{index=5}
					"서울시 관악구 관악로 255-7", // :contentReference[oaicite:6]{index=6}
					"서울시 관악구 관악로 285", // :contentReference[oaicite:7]{index=7}
					"서울시 관악구 관악로 287", // :contentReference[oaicite:8]{index=8}
					"서울시 관악구 관악로 133", // :contentReference[oaicite:9]{index=9}
					"서울시 관악구 관악로 85", // :contentReference[oaicite:10]{index=10}
					"서울시 관악구 관악로 146", // :contentReference[oaicite:11]{index=11}
					"서울시 관악구 관악로 148", // :contentReference[oaicite:12]{index=12}
					"서울시 관악구 관악로 152", // :contentReference[oaicite:13]{index=13}
					"서울시 관악구 관악로 154-6", // :contentReference[oaicite:14]{index=14}
					"서울시 관악구 관악로 154-10", // :contentReference[oaicite:15]{index=15}
					"서울시 관악구 관악로 154-12", // :contentReference[oaicite:16]{index=16}
					"서울시 관악구 관악로 154-14", // :contentReference[oaicite:17]{index=17}
					"서울시 관악구 관악로 154-16", // :contentReference[oaicite:18]{index=18}
					"서울시 관악구 관악로 154-5", // :contentReference[oaicite:19]{index=19}
					"서울시 관악구 관악로 154", // :contentReference[oaicite:20]{index=20}
					"서울시 관악구 관악로 140-18", // :contentReference[oaicite:21]{index=21}
					"서울시 관악구 관악로 140-16", // :contentReference[oaicite:22]{index=22}
					"서울시 관악구 관악로 132", // :contentReference[oaicite:23]{index=23}
					"서울시 관악구 관악로 134", // :contentReference[oaicite:24]{index=24}
					"서울시 관악구 관악로 136", // :contentReference[oaicite:25]{index=25}
					"서울시 관악구 관악로 140-10", // :contentReference[oaicite:27]{index=27}
					"서울시 관악구 관악로 140-6", // :contentReference[oaicite:28]{index=28}
					"서울시 관악구 관악로 140-4", // :contentReference[oaicite:29]{index=29}
					"서울시 관악구 관악로 140", // :contentReference[oaicite:30]{index=30}
					"서울시 관악구 관악로 140-7", // :contentReference[oaicite:31]{index=31}
					"서울시 관악구 관악로 140-9", // :contentReference[oaicite:32]{index=32}
					"서울시 관악구 관악로 140-11", // :contentReference[oaicite:33]{index=33}
					"서울시 관악구 관악로 140-15", // :contentReference[oaicite:34]{index=34}
					"서울시 관악구 관악로 144", // :contentReference[oaicite:35]{index=35}
					"서울시 관악구 관악로 126", // :contentReference[oaicite:36]{index=36}
					"서울시 관악구 관악로 118", // :contentReference[oaicite:37]{index=37}
					"서울시 관악구 관악로 120", // :contentReference[oaicite:38]{index=38}
					"서울시 관악구 관악로 122", // :contentReference[oaicite:39]{index=39}
					"서울시 관악구 관악로 124", // :contentReference[oaicite:40]{index=40}
					"서울시 관악구 관악로 128", // :contentReference[oaicite:41]{index=41}
					"서울시 관악구 관악로 112-9", // :contentReference[oaicite:42]{index=42}
					"서울시 관악구 관악로 106", // :contentReference[oaicite:43]{index=43}
					"서울시 관악구 관악로 108", // :contentReference[oaicite:44]{index=44}
					"서울시 관악구 관악로 110", // :contentReference[oaicite:45]{index=45}
					"서울시 관악구 관악로 110-1", // :contentReference[oaicite:46]{index=46}
					"서울시 관악구 관악로 112-7", // :contentReference[oaicite:47]{index=47}
					"서울시 관악구 관악로 112", // :contentReference[oaicite:48]{index=48}
					"서울시 관악구 관악로 114", // :contentReference[oaicite:49]{index=49}
					"서울시 관악구 관악로 116", // :contentReference[oaicite:50]{index=50}
					"서울시 관악구 관악로 96", // :contentReference[oaicite:51]{index=51}
					"서울시 관악구 관악로 98", // :contentReference[oaicite:52]{index=52}
					"서울시 관악구 관악로 100", /// :contentReference[oaicite:53]{index=53}
					"서울시 관악구 관악로 102", /// :contentReference[oaicite:54]{index=54}
					"서울시 관악구 관악로 102-1", // :contentReference[oaicite:55]{index=55}
					"서울시 관악구 관악로 104", // :contentReference[oaicite:56]{index=56}
					"서울시 관악구 관악로 103-3", /// :contentReference[oaicite:57]{index=57}
					"서울시 관악구 관악로 97", /// :contentReference[oaicite:58]{index=58}
					"서울시 관악구 관악로 103-10", // :contentReference[oaicite:59]{index=59}
					"서울시 관악구 관악로 117", /// :contentReference[oaicite:60]{index=60}
					"서울시 관악구 관악로 115", // :contentReference[oaicite:61]{index=61}
					"서울시 관악구 관악로 113", // :contentReference[oaicite:62]{index=62}
					"서울시 관악구 관악로 113-1", // :contentReference[oaicite:63]{index=63}
					"서울시 관악구 관악로 111", // :contentReference[oaicite:64]{index=64}
					"서울시 관악구 관악로 109", // :contentReference[oaicite:65]{index=65}
					"서울시 관악구 관악로 107", // :contentReference[oaicite:66]{index=66}
					"서울시 관악구 관악로 105", // :contentReference[oaicite:67]{index=67}
					"서울시 관악구 관악로 103-6", // :contentReference[oaicite:68]{index=68}
					"서울시 관악구 관악로 103-12", // :contentReference[oaicite:69]{index=69}
					"서울시 관악구 관악로 125", // :contentReference[oaicite:70]{index=70}
					"서울시 관악구 관악로 139", // :contentReference[oaicite:71]{index=71}
					"서울시 관악구 관악로 137", // :contentReference[oaicite:72]{index=72}
					"서울시 관악구 관악로 145", // :contentReference[oaicite:73]{index=73}
					"서울시 관악구 관악로 302", // :contentReference[oaicite:74]{index=74}
					"서울시 관악구 관악로 304", // :contentReference[oaicite:75]{index=75}
					"서울시 관악구 관악로 306", // :contentReference[oaicite:76]{index=76}
					"서울시 관악구 남부순환로 1427", // :contentReference[oaicite:77]{index=77}
					"서울시 관악구 남부순환로 1546", // :contentReference[oaicite:78]{index=78}
					"서울시 관악구 남부순환로 1914", // :contentReference[oaicite:79]{index=79}
					"서울시 관악구 봉천로 167", // :contentReference[oaicite:80]{index=80}
					"서울시 관악구 봉천로 415-1", // :contentReference[oaicite:81]{index=81}
					"서울시 관악구 봉천로 279-7", // :contentReference[oaicite:82]{index=82}
					"서울시 관악구 낙성대로 2", // :contentReference[oaicite:83]{index=83}
					"서울시 관악구 낙성대로 4", // :contentReference[oaicite:84]{index=84}
					"서울시 관악구 낙성대로 7", // :contentReference[oaicite:85]{index=85}
					"서울시 관악구 낙성대로 8", // :contentReference[oaicite:86]{index=86}
					"서울시 관악구 낙성대로 9", // :contentReference[oaicite:87]{index=87}
					"서울시 관악구 양녕로1길 46", // 봉천동 ([도로명주소 사이트]:contentReference[oaicite:1]{index=88})
					"서울시 관악구 남부순환로 1679", // 봉천동 조은빌딩 ([도로명주소 사이트]:contentReference[oaicite:2]{index=89})
					"서울시 관악구 관악로 208-18", // 봉천동 ([주소 영문 변환]:contentReference[oaicite:3]{index=90})
					"서울시 관악구 관악로 153", // 봉천동 ([도로명 목록]:contentReference[oaicite:4]{index=91})
					"서울시 관악구 관악로 155-2", // 봉천동 ([도로명 목록]:contentReference[oaicite:5]{index=92})
					"서울시 관악구 관악로 161", // 봉천동 ([도로명 목록]:contentReference[oaicite:6]{index=93})
					"서울시 관악구 관악로 161-1", // 봉천동 ([도로명 목록]:contentReference[oaicite:7]{index=94})
					"서울시 관악구 관악로 157", // 봉천동 ([도로명 목록]:contentReference[oaicite:8]{index=95})
					"서울시 관악구 관악로 165-2", // 봉천동 ([도로명 목록]:contentReference[oaicite:9]{index=96})
					"서울시 관악구 관악로 173-2", // 봉천동 ([도로명 목록]:contentReference[oaicite:10]{index=97})
					"서울시 관악구 관악로 173-3", // 봉천동 ([도로명 목록]:contentReference[oaicite:11]{index=98})
					"서울시 관악구 관악로 175-2", // 봉천동 ([도로명 목록]:contentReference[oaicite:12]{index=99})
					"서울시 관악구 관악로 183", // 봉천동 ([도로명 목록]:contentReference[oaicite:13]{index=100})

			};
			dto.setMemberAddress(addresses[i - 1]);

			// 4. 주소 -> 좌표 변환
			try {
				Map<String, Double> map = this.kakaoLocalService.getCoordinates(dto.getMemberAddress());
				if (map != null) {
					dto.setLongitude(map.get("X")); // 경도
					dto.setLatitude(map.get("Y")); // 위도
				}
			} catch (Exception igonore) {

			}

			// 5. 저장 후, 저장된 엔티티 반환

			memberRepository.save(dto);
		}
	}

	// [중복 확인] AJAX용
	// DB에 해당 값이 존재하면 "사용 불가", 없으면 "사용 가능"
	// 컨트롤러 checkId()
	public boolean isIdAvailable(String memberId) {
		return memberRepository.findByMemberId(memberId).isEmpty();
	}

	// 컨트롤러 checkEmail()
	public boolean isEmailAvailable(String email) {
		return memberRepository.findByMemberEmail(email).isEmpty();
	}

	// 컨트롤러 checkNick()
	public boolean isNickAvailable(String nick) {
		return memberRepository.findByMemberNick(nick).isEmpty();
	}

	// [로그인] 사용자 조회
	// 컨트롤러 login()
	// 아이디로 회원 1명 조회, 없으면 null
	public MemberDTO getMember(String memberId) {
		return memberRepository.findByMemberId(memberId).orElse(null);
	}

	// [로그인] 비밀번호 확인
	// 컨트롤러 login(POST), checkPassword(POST)
	// 입력 비번을 trim -> 저장된 해시와 matches()로 검증
	// 매개변수 : memberId, memberPw
	// 반환 : true/false
	public boolean checkPassword(String memberId, String memberPw) {
		String inputPw = memberPw.trim(); // 입력값 공백 제거
		return memberRepository.findByMemberId(memberId)
				.map(member -> passwordEncoder.matches(inputPw, member.getMemberPw()))
				.orElse(false);
	}

	// [로그인] 임시 비밀번호 로그인 체크
	// 컨트롤러 login(POST)
	// isTemporary == 1 이면 임시 비번 상태 -> 비번 변경 페이지로 유도
	public boolean isTemporaryPassword(MemberDTO dto) {
		return dto != null && dto.getIsTemporary() != null && dto.getIsTemporary() == 1;
	}

	// [자동 로그인] 토큰 저장
	// 컨트롤러 login(POST)
	// 회원에게 랜덤 토큰 발급 -> DB 저장 -> 컨트롤러에서 쿠키로 내려줌
	// 매개변수 : memberId, token
	@Transactional
	public void saveAutoLoginToken(String memberId, String token) {
		MemberDTO dto = memberRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
		dto.setAutoLoginToken(token); // DTO에 토큰 세팅
		memberRepository.save(dto); // DB에 저장
		// 토큰이 DB에 반영됨
	}

	// [자동 로그인] 토큰으로 회원 조회
	// 컨트롤러 login(GET)
	// 브라우저 쿠키에 담겨 온 토큰을 DB에서 찾아 회원을 되살림 (자동 로그인)
	// 매개변수 : token
	public MemberDTO getMemberByAutoLoginToken(String token) {
		return memberRepository.findByAutoLoginToken(token).orElse(null);
	}

	// [아이디 찾기] 이름 + 이메일로 회원 1명 조회
	// 컨트롤러 findId(POST)
	// 일치하는 회원이 없으면 null
	// 매개변수 : memberName, memberEmail
	public MemberDTO findMemberId(String memberName, String memberEmail) {
		Optional<MemberDTO> memberOpt = memberRepository.findByMemberNameAndMemberEmail(memberName, memberEmail);
		return memberOpt.orElse(null);
	}

	// [비밀번호 찾기] 회원 정보 확인
	// 컨트롤러 findPw(POST)
	// 아이디+이름+이메일 모두 일치하는지 확인
	// 매개변수 : memberId, memberName, memberEmail, memberDTO
	public MemberDTO findMemberByIdNameEmail(String memberId, String memberName, String memberEmail) {
		return memberRepository
				.findByMemberIdAndMemberNameAndMemberEmail(memberId, memberName, memberEmail)
				.orElse(null);
	}

	// [비밀번호 찾기] 임시 비밀번호 생성
	// 컨트롤러 findPw(POST)
	// 임시 비밀번호 랜덤 생성 (영문+숫자, 8자리)
	// 매개변수 : String - 생성된 임시 비밀번호
	public String generateTempPassword() {
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			int idx = random.nextInt(chars.length());
			sb.append(chars.charAt(idx));
		}
		return sb.toString();
	}

	// [비밀번호 찾기] 입력한 이메일로 임시 비밀번호 전송
	// 컨트롤러 findPw(POST)
	// 실패 시 IllegalStateException 던져서 컨트롤러에서 사용자 메시지 처리
	// 매개변수 : memberEmail, tempPw
	public void sendTempPasswordEmail(String memberEmail, String tempPw) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(memberEmail);
		message.setSubject("임시 비밀번호 발급 안내"); // 제목
		message.setText("회원님의 임시 비밀번호는 아래와 같습니다.\n\n" + tempPw + "\n\n로그인 후 반드시 비밀번호를 변경해주세요."); // 내용

		try {
			mailSender.send(message);
		} catch (Exception e) {
			throw new IllegalStateException("임시 비밀번호 이메일 전송 실패", e);
		}
	}

	// [비밀번호 찾기]
	// 컨트롤러 findPw(POST)
	// DB에 임시 비밀번호 저장
	// 찾아진 회원의 비밀번호를 임시 비번으로 교체 : isTemporary=1 설정
	// 반드시 암호화해서 저장
	@Transactional
	public void updatePassword(String memberId, String tempPw) {
		MemberDTO dto = memberRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

		// 반드시 암호화 후 저장
		String encodedPw = passwordEncoder.encode(tempPw.trim());
		dto.setMemberPw(encodedPw);
		dto.setIsTemporary(1); // 임시 비번 상태 진입
		memberRepository.save(dto);

		System.out.println("임시 비밀번호 DB 저장 완료 : " + encodedPw);
	}

	// [비밀번호 변경]
	// 컨트롤러 changePassword(POST)
	// 로그인 후, 임시 비번 상태에서 새 비번으로 교체
	// 매개변수 : memberId, newPw
	// 동작
	// 1. 회원 조회
	// 2. 새 비밀번호 암호화 후 저장
	// 3. isTemporary = 0 해제
	@Transactional
	public void changePassword(String memberId, String newPw) {
		// 회원 조회 (없으면 예외 발생)
		MemberDTO dto = memberRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

		// 새 비밀번호 암호화 후 세팅
		String encodedPw = passwordEncoder.encode(newPw.trim());
		dto.setMemberPw(encodedPw);

		// 임시 비밀번호 해제
		dto.setIsTemporary(0);

		// DB에 저장
		memberRepository.save(dto);
	}

	// [마이페이지] 회원 정보 수정
	// 컨트롤러 updateMember(GET)
	// 이름/전화번호 변경, 새 비번이 입력되면 암호화 후 교체
	// 아이디 기준으로 대상 회원 조회 후 수정
	public void updateMember(MemberDTO dto) {
		MemberDTO member = memberRepository.findByMemberId(dto.getMemberId())
				.orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

		// 이름, 전화번호 수정
		member.setMemberName(dto.getMemberName());
		member.setMemberPhone(dto.getMemberPhone());

		// 새 비밀번호가 입력된 경우에만 변경
		if (dto.getMemberPw() != null && !dto.getMemberPw().isEmpty()) {
			member.setMemberPw(passwordEncoder.encode(dto.getMemberPw().trim()));
		}

		// DB에 저장
		memberRepository.save(member);
	}
	
	// [마이페이지] 회원 정보 수정
	// 전화번호 유효성 검사
	// DB에 조회된 전화번호와 일치하면 수정하지 못하도록 하기 위함
	public MemberDTO getMemberByPhone(String memberPhone) {
		return memberRepository.findByMemberPhone(memberPhone).orElse(null);
	}

	// [회원 탈퇴]
	// 탈퇴 사유 저장 : memberDeleteReason
	// 권한/상태 전환 : memberRole = 3 (탈퇴 회원)
	// 자동 로그인 토큰 제거 , 임시 비번 상태 초기화
	// 실제로 삭제하지 않음 (DB에 memberRole = 3 이 되어 남아있음)
	@Transactional
	public void withdrawMember(String memberId, String reasonSummary) {
		MemberDTO m = memberRepository.findByMemberId(memberId)
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

		// 탈퇴 사유 저장
		m.setMemberDeleteReason(reasonSummary); // ex) "서비스 불편, 기타: ~~"

		// 상태를 '탈퇴회원'으로 전환
		m.setMemberRole(3); // 0:USER, 1:SELLER, 2=ADMIN, 3=탈퇴회원, 4=RIDER

		// 자동 로그인 토큰 등 민감 상태 해제
		m.setAutoLoginToken(null);

		// 임시 비밀번호 상태 초기화
		// if(m.getIsTemporary() != null && m.getIsTemporary() != 0){
		// m.setIsTemporary(0);
		// }

		// 재가입/중복방지 정책 -> 이메일/전화/닉네임에 suffix를 붙이거나 마스킹
		// 유니크 제약 때문에 신규 가입을 막지 않음
		m.setMemberEmail(m.getMemberEmail() + ".withdrawn." + UUID.randomUUID());
		m.setMemberNick(m.getMemberNick() + "_탈퇴");
	}

	// 탈퇴 회원 제외하고 아이디로 회원 가져오기
	public MemberDTO getActiveMember(String memberId) {
		return memberRepository.findActiveById(memberId).orElse(null);
	}

	// 탈퇴 회원 제외하고 비밀번호 검증
	public boolean checkActivePassword(String memberId, String rawPw) {
		return memberRepository.findActiveById(memberId)
				.map(m -> passwordEncoder.matches(rawPw.trim(), m.getMemberPw()))
				.orElse(false);
	}
}