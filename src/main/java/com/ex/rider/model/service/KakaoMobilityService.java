package com.ex.rider.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 카카오 모빌리티 서비스
// 길찾기 (Directions)
// 예상 소요 시간 , 경로 최적화 서비스 제공
@Service
public class KakaoMobilityService {

    private static final String KAKAO_API_URL = "https://apis-navi.kakaomobility.com/v1/directions";

    private final RestTemplate restTemplate;

    @Value("${kakao.api.key}")
    private String apiKey; // application.yml / properties 에 설정

    public KakaoMobilityService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 카카오 모빌리티 Directions API를 사용하여
     * 출발지(origin)와 목적지(destination) 사이의 주행 거리/예상 소요 시간을 계산하는 메서드
     *
     * @param originLongitude 출발지 경도 (longitude, X축)
     * @param originY         출발지 위도 (latitude, Y축)
     *                        Latitude* @param destX 목적지 경도
     *                        (longLongdestLongitudetude, X축)
     * @param destLatitude    목적지 위도 (latitude, Y축)
     * @return Map<String, Object>
     *         - "distanceKm" : 거리 (현재는 m 단위, 후처리로 /1000 하여 km 단위 변환 가능)
     *         - "durationMin": 예상 소요 시간 (분 단위)
     *         - "raw" : 카카오 API에서 받은 원본 JSON 응답 전체
     *
     *         동작 과정:
     *         1. API 요청 URL 구성
     *         - GET https://apis-navi.kakaomobility.com/v1/directions
     *         - origin={경도,위도,name=출발지}
     *         - destination={경도,위도,name=목적지}
     *         - 추가 옵션(getOptions())도 함께 붙여줌
     *
     *         2. Authorization 헤더에 Kakao REST API Key 추가
     *
     *         3. RestTemplate을 이용해 API 호출 → Map으로 응답 파싱
     *
     *         4. 응답 검증
     *         - body가 null이거나 "routes" 키가 없으면 "길찾기 실패" 예외 발생
     *
     *         5. 응답 파싱
     *         - routes[0].summary.distance → 거리 (m 단위)
     *         - routes[0].summary.duration → 시간 (초 단위)
     *
     *         6. 결과를 Map에 담아 반환
     *         - distanceKm : 거리(m) → 필요 시 /1000 하여 km 변환
     *         - durationMin : 소요 시간(분)
     *         - raw : 전체 응답(JSON Map)
     */
    public Map<String, Object> getDirections(double originLongitude, double originLatitude,
            double destLongitude, double destLatitude) {
        // origin: 출발지의 경도,위도 (X,Y)
        // destination: 목적지의 경도,위도 (X,Y)
        // options() : 따로 옵션들만 설정해서 url에 더해줌
        String url = KAKAO_API_URL +
                "?origin=" + originLongitude + "," + originLatitude + ",name=출발지" +
                "&destination=" + destLongitude + "," + destLatitude + ",name=목적지" + getOptions();

        // 1. 요청 헤더에 인증키 추가
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + apiKey);

        // 2. API 호출
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        // 3. 응답 검증
        Map<String, Object> body = response.getBody();
        if (body == null || !body.containsKey("routes")) {
            throw new RuntimeException("길찾기 실패: 응답 없음");
        }

        // 4. 첫 번째 경로의 summary 정보 추출
        List<Map<String, Object>> routes = (List<Map<String, Object>>) body.get("routes");
        if (routes.isEmpty() || routes.get(0).get("summary") == null) {
            throw new RuntimeException("길찾기 실패: summary 없음 (origin=" + originLatitude + "," + originLongitude +
                    ", dest=" + destLatitude + "," + destLongitude + ")");
        }
        Map<String, Object> summary = (Map<String, Object>) routes.get(0).get("summary");

        // 5. 거리/시간 파싱
        double distance = Double.parseDouble(summary.get("distance").toString()); // m 단위
        double duration = Double.parseDouble(summary.get("duration").toString()); // 초 단위

        // 6. 최종 결과 구성
        Map<String, Object> result = new HashMap<>();
        result.put("distance", distance); // 현재 m 단위 → /1000 하면 km
        result.put("durationMin", Math.round(duration / 60)); // 분 단위
        result.put("raw", body); // 원본 응답 JSON도 같이 반환

        return result;
    }

    public String getOptions() {
        // 기본 API 요청 호출 주소

        // 필수 파라미터
        // 1. 출발지 (origin)
        // X좌표(경도,Longitude),Y좌표(위도,Latitude) 순으로 사용
        // name="출발지"을 사용하여 출발지의 이름을 지정해 줄 수 있음
        String origin = "origin=경도,위도,name=출발지";

        // 2. 목적지 (destination)
        // X좌표(경도,Longitude),Y좌표(위도,Latitude) 순으로 사용
        // name="목적지"을 사용하여 출발지의 이름을 지정해 줄 수 있음
        String destination = "&destination=경도,위도,name=목적지";

        // 선택 파라미터 (원하는 옵션 추가 가능)
        // 1. 경유지 (waypoints) waypoints=경유지1의X,경유지1의Y,name=경유지1
        // 경유지는 최대 5개 까지 지정 가능
        // 여러 개일 경우 "|" 또는 "%7C" 로 구분
        String waypoints = "&waypoints=127.115,37.393,name=경유지1|127.117,37.395,name=경유지2";

        // 2. 경로 탐색 우선순위 (priority)
        // 설정 안할시 RECOMMEND (기본값:추천 경로)
        // - RECOMMEND (추천 경로)
        // - TIME (최단 시간)
        // - DISTANCE (최단 거리)
        String priority = "&priority=TIME"; // 최단 시간 기준

        // 3. avoid (회피 옵션)
        // - motorway (자동차 전용 도로)
        // - toll (유료 도로)
        // - ferries (페리 항로)
        // - schoolzone (어린이 보호구역)
        // - uturn (유턴)
        // → 여러 개는 '|' 로 연결 가능
        String avoid = "&avoid=smotorway|ferrie"; // 고속도로, 페리 회피

        // 4. roadevent (유고 정보 반영 여부)
        // 설정 안할 시 0 기본값
        // - 0 (기본값: 도로 통제 전부 반영)
        // - 1 (출발/도착지 주변 통제는 반영 안 함)
        // - 2 (모든 도로 통제 반영 안 함)
        String roadevent = "&roadevent=0"; // 기본값: 도로 통제 반영

        // 5. alternatives (대체 경로 제공 여부)
        // - true (대체 경로도 함께 제공)
        // - false (기본 경로만 제공, 기본값)
        String alternatives = "&alternatives=true"; // 대체 경로 제공

        // 6. road_details (상세 도로 정보 제공 여부)
        // - true (상세 도로 정보 포함)
        // - false (기본값, 포함하지 않음)
        String road_details = "&road_details=true"; // 상세 도로 정보

        // 7. car_type (차종)
        // - 1 (승용차, 기본값)
        // - 2 (버스)
        // - 3 (택시)
        // - 4 (화물차)
        // - 5 (이륜차)
        String car_type = "&car_type=5"; // 이륜차

        // 8. car_fuel (연료 종류)
        // - GASOLINE (휘발유, 기본값)
        // - DIESEL (경유)
        // - LPG (LPG)
        String car_fuel = "&car_fuel=GASOLINE"; // 휘발유

        // 9. car_hipass (하이패스 장착 여부)
        // - true (하이패스 장착)
        // - false (미장착, 기본값)
        String car_hipass = "&car_hipass=true"; // 하이패스 장착

        // 10. summary (요약 정보만 제공 여부)
        // - true (요약 정보만 제공)
        // - false (상세 정보 포함, 기본값)
        String summary = "&summary=true"; // 요약정보 X, 상세 포함

        // 사용할 옵션들 합쳐서 리턴
        // 경로 우선 순위 , 차종 만 사용
        String options = priority + car_type;
        return options;
    }
}
