package com.ex.rider.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// 카카오 로컬 서비스 
// 주소 -> 좌표 변환 (Geocoding)
// 좌표 -> 주소 변환 (Reverse Geiciding)
// 키워드 장소 검색 , 카테고리 장소 검색 등 서비스 사용
@Service
public class KakaoLocalService {

    private static final String KAKAO_LOCAL_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestTemplate restTemplate;

    @Value("${kakao.api.key}")
    private String apiKey;

    public KakaoLocalService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 카카오 로컬 API를 사용하여 주소 문자열을 위도(lat), 경도(lon) 좌표로 변환하는 메서드
     *
     * @param address 변환하고자 하는 전체 주소 (예: "경기 부천시 소사로 327번길 15")
     * @return Map<String, Double> - 좌표 정보
     *         key: "longitude" → 경도 (longitude, x축)
     *         key: "latitude" → 위도 (latitude, y축)
     *
     *         동작 과정:
     *         1. 카카오 로컬 API의 주소검색 엔드포인트 호출
     *         - GET https://dapi.kakao.com/v2/local/search/address.json?query={주소}
     *         - Authorization 헤더에 Kakao REST API Key 필요
     *
     *         2. API 응답 파싱
     *         - body.documents 배열에서 첫 번째 결과를 꺼냄
     *         - documents[0].longitude → 경도(longitude)
     *         - documents[0].latitude → 위도(latitude)
     *
     *         3. 변환 결과를 Map에 담아 반환
     *
     *         예외 처리:
     *         - 응답 body가 null인 경우 → "좌표 변환 실패: 응답 없음" 예외 발생
     *         - 결과 documents가 비어 있는 경우 → "좌표 변환 실패: 결과 없음" 예외 발생
     */
    public Map<String, Double> getCoordinates(String address) {
        String query = address.replace("(\\d+\\s*)", "$1").trim();
        String url = KAKAO_LOCAL_URL + "?query=" + query;

        // 1. 요청 헤더에 인증키 추가 (Kakao REST API Key)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);

        // 2. API 호출 (RestTemplate 이용)
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        // 3. 응답 검증 (body 확인)
        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("documents")) {
            throw new RuntimeException("좌표 변환 실패: 응답 없음");
        }

        // 4. documents 배열 추출
        List<Map<String, Object>> docs = (List<Map<String, Object>>) body.get("documents");
        if (docs.isEmpty()) {
            throw new RuntimeException("좌표 변환 실패: 결과 없음");
        }

        // 5. 첫 번째 좌표 데이터 가져오기
        Map<String, Object> info = docs.get(0);
        double longitude = Double.parseDouble(info.get("x").toString()); // 경도
        double latitude = Double.parseDouble(info.get("y").toString()); // 위도

        // 6. 최종 결과 Map에 담아 반환
        Map<String, Double> result = new HashMap<>();
        result.put("longitude", longitude); // 경도 X
        result.put("latitude", latitude); // 위도 Y
        return result;
    }
}
