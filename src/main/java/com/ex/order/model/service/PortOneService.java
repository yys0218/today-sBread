package com.ex.order.model.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ex.order.model.data.ScheduleAnnotation;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class PortOneService {
	
	//관리자 콘솔 - 연동 정보 - 식별코드/API Keys에서 확인 가능
	@Value("${imp.api.key}")
	private String apiKey;
	
	@Value("${imp.api.secretkey}")
	private String secretKey;
	
	//토큰 발급 메서드
	public String getToken() { //service 내의 변수를 알아서 사용하도록 함
		
		try {//입출력 스트림 사용시 예외처리 필수
			//1. 토큰 발급 url 객체 생성 >> 해당 url로 내 apiKey, secretKey가 전송되어야함
			URL url = new URL("https://api.iamport.kr/users/getToken");
			//2. (https프로토콜로 형변환) url을 서버와 연결
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			//http요청 방식 설정
			conn.setRequestMethod("POST"); //post방식으로 요청
			conn.setRequestProperty("Content-Type", "application/json"); //contentType 설정: 요청 본문이 JSON형식임을 알림
			conn.setRequestProperty("Accept", "application/json"); //Accept 헤더 설정: 응답을 JSON형식으로 받고싶다고 알림
			conn.setDoOutput(true); //서버에 데이터를 보낼 수 있도록 출력 스트림 사용 가능하게 설정
			
			//3. 서버로 보낼 데이터 변환
			//apiKey, secretKey를 JSON형식으로 변환
			Map<String,String> json = new HashMap<String, String>(); //Map타입 객체 생성
			json.put("imp_key", apiKey); //"imp_key"에 apiKey
			json.put("imp_secret", secretKey); //"imp_secret"에 secretKey
			
			ObjectMapper objectMapper = new ObjectMapper(); //자바 객체를 Json문자열로 변환
			String jsonString = objectMapper.writeValueAsString(json); //출력 스트림으로 서버에 보낼 데이터
			
			//4. 서버(conn)로 데이터를 보내는 통로(OutputStream) 생성&보내기
			//BufferedWriter : 문자열 처리에 적합한 출력보조?스트림, 버퍼에 데이터를 저장했다가 한 번에 출력함
			//OutputStreamWriter : 문자(char)를 바이트로 변환하여 출력, writer와 outputStream을 연결함
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			
			bw.write(jsonString); //http요청 본문에 jsonString 출력
			bw.flush(); //버퍼 비우기
			bw.close(); //스트림 사용 종료
			
			//5. 서버로부터 응답 받기 
			//응답 받을 스트림 생성
			//BufferedReader : 대량의 문자열 처리에 적합한 입력보조?스트림, 데이터를 버퍼에 저장했다가 한 번에 읽음
			//InputStreamReader : 바이트를 문자로 변환하여 읽음, reader와 inputStream을 연결
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			//JSON객체로 돌아오는 응답을 자바 객체로 변환
			Map<String, Object> responseMap = objectMapper.readValue(br.readLine(), Map.class); //br로 받은 응답을 Map타입으로 변환
			Map<String, Object> tokenMap = (Map<String,Object>)responseMap.get("response"); //Map타입 응답에서 Map타입 response 꺼내기
			String accessToken = tokenMap.get("access_token").toString(); //tokenMap에서 액세스 토큰 꺼내기
			
			br.close(); //스트림 사용 종료
			conn.disconnect(); //서버와 맺은 http연결 종료
			
			return accessToken;
			
		} catch (Exception e) {
			e.getStackTrace();
			return "토큰 생성 실패";
		}
	}

	//결제 취소하는 메서드
	public void getRefund(String accessToken, String merchantUid) {
		
		try {//입출력 스트림 사용시 예외처리 필수
			//1. 결제 취소 url객체 생성 >> accessToken, merchantUid 전송 필요
			URL url= new URL("https://api.iamport.kr/payments/cancel");

			//2. url객체를 서버(RestAPI)와 연결 (https프로토콜 사용)
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			//http요청 방식 설정
			conn.setRequestMethod("POST"); //POST방식 전송
			conn.setRequestProperty("Content-Type", "application/json"); //JSON방식으로 데이터 전송
			conn.setRequestProperty("Accept", "application/json"); //JSON방식으로 응답 받기
			conn.setRequestProperty("Authorization", accessToken); //발급받은 access토큰으로 권한 부여
			conn.setDoOutput(true); //서버로 데이터 보낼 때 출력 스트림 사용가능하도록 설정
			
			//3. 서버로 보낼 데이터 변환
			//merchantUid를 JSON형식으로 변환 (amount는 부분환불에 필요, 미입력 시 전액 환불) >> 일단 reason없이 가본다~!
			Map<String,String> json = new HashMap<String,String>(); //Map타입 객체 생성
			json.put("merchant_uid", merchantUid);
			
			ObjectMapper objectMapper = new ObjectMapper(); //JSON객체로 변환해주는 객체 생성
			String jsonString = objectMapper.writeValueAsString(json);//json을 JSON문자열로 변환
			
			//4. 서버로의 출력스트림 생성& 데이터 출력
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())); //출력 스트림 생성
			bw.write(jsonString); //데이터 출력
			bw.flush(); //버퍼 비우기
			bw.close(); //스트림 사용 종료
			
			//5. 서버로부터 응답 받을 입력스트림 생성
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); //입력 스트림 생성
			//이렇게 입력스트림만 있는데 응답은 알아서 받아지는 건가..?메서드 리턴타입도 없는데...			
			
			//6. 로그로 응답의 body확인 (성공여부 확인용)			
			Map<String, Object> responseMap = objectMapper.readValue(br.readLine(),Map.class);//응답을 Map으로 받기
			String responseCode = responseMap.get("code").toString(); //응답 코드 꺼내기
			String responseMessage = responseMap.get("message").toString(); //응답 메세지 꺼내기
			System.out.println("코드: "+responseCode);
			System.out.println("메세지: "+responseMessage);

			br.close(); //스트림 사용 종료
			conn.disconnect(); //http프로토콜 사용 종료		
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//비인증 결제 연동 (빌링키 결제 API 호출)
	//매개변수: 액세스토큰, merchantUid, customerUid
	public Map<String,Object> subscriptionOrder(String accessToken, String merchantUid, String customerUid, int amount, String name){

		try {
			//1. 빌링키 결제 api 호출할 URL 객체 생성
			URL url = new URL("https://api.iamport.kr/subscribe/payments/again");
			//2. url을 서버와 연결 (https프로토콜로 형변환)
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			//http요청 방식 설정
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json"); //전송 데이터 형식 json
			conn.setRequestProperty("Accept","application/json"); //응답 데이터 형식 json
			conn.setRequestProperty("Authorization", accessToken);//api호출 권한으로 발급받은 accessToken
			conn.setDoOutput(true); //서버에 출력스트림 사용
			
			//3. 서버로 보낼 데이터 변환 (merchant~, customer~를 JSON으로)
			//Map타입 객체 생성
			Map<String, Object> json = new HashMap<String, Object>(); 
			json.put("merchant_uid", merchantUid);
			json.put("customer_uid", customerUid);
			json.put("amount", amount);
			json.put("name", name);
			
			//Map >> json으로 변환
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(json); //json문자열
			
			//4. 서버로 데이터 보내는 출력스트림 생성&전송
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			//url로 jsonString 출력(전송)
			bw.write(jsonString);
			bw.flush();//버퍼 비우기
			bw.close(); //스트림 사용 종료
			
			//5. 서버로부터 응답 받기 (응답받을 입력 스트림 생성)
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			//JSON형식의 응답 자바 객체로 변환
			Map<String, Object> responseMap = objectMapper.readValue(br.readLine(),Map.class);
			
			return responseMap;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
		
	//결제예약 API호출 
	//매개변수: 액세스토큰, merchantUid, customerUid, ScheduledAt(다음 결제 예정 시각)
	public Map<String, Object> scheduleOrder(String accessToken, String customerUid, ScheduleAnnotation[] schedules){
		
		try {
			//1. 결제예약API URL객체 생성
			URL url = new URL("https://api.iamport.kr/subscribe/payments/schedule");
			//2. url을 서버와 연결(https 프로토콜 형변환)
			HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
			//http요청 방식 설정
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type","application/json");
			conn.setRequestProperty("Accept", "application/json");
			conn.setRequestProperty("Authorization", accessToken);
			conn.setDoOutput(true); //서버에 출력스트림 사용
			
			//3. 서버로 보낼 데이터 JSON문자열로 변환
			Map<String, Object> json = new HashMap<String,Object>();
			json.put("customer_uid", customerUid);
			json.put("schedules", schedules);
			
			//Map>>json으로 변환
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(json);
			
			//4. 서버로 데이터 보내는 출력 스트림 생성&전송
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			bw.write(jsonString);
			bw.flush();
			bw.close();
			
			//5. 서버로부터 응답 받을 입력 스트림 생성
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			//응답)json>>Map으로 변환
			Map<String, Object> responseMap = objectMapper.readValue(br.readLine(), Map.class);
			
			return responseMap;
			
		} catch (Exception e) {
			e.printStackTrace();
			//예외 발생 시 리턴할 Map객체
			Map<String, Object> errorMap = new HashMap<String, Object>();
			errorMap.put("code", "-1"); //코드 임의 지정
			errorMap.put("message",e.toString()); //오류 스크립트
			return errorMap;
		}
	}
	
	// 작업자 : 안성진
	// 수정 예정
	// 정기 결제 취소할 때 필요한 메서드
	public void deleteBillingKey(String token, String customerUid) {
		// TODO Auto-generated method stub
		
	}

}
