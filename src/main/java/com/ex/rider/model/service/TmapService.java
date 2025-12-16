package com.ex.rider.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TmapService {

    private static final String TMAP_API_URL = "https://apis.openapi.sk.com/tmap/geo/fullAddrGeo";
    private static final String APP_KEY = "HF54XgP4IDasImIzZ2bEPahklEFoB35z75mPPfbE"; // 발급받은 Tmap AppKey

    public Map<String, Double> getCoordinates(String address) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 파라미터
        String url = TMAP_API_URL +
                "?version=1&format=json&coordType=WGS84GEO&fullAddr=" + address +
                "&appKey=" + APP_KEY;

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);

        // 응답 파싱
        Map body = response.getBody();
        if (body == null || !body.containsKey("coordinateInfo")) {
            throw new RuntimeException("좌표 변환 실패: 응답 없음");
        }

        Map coordinateInfo = (Map) body.get("coordinateInfo");
        List<Map<String, Object>> coordinates = (List<Map<String, Object>>) coordinateInfo.get("coordinate");

        if (coordinates == null || coordinates.isEmpty()) {
            throw new RuntimeException("좌표 변환 실패: 좌표 데이터 없음");
        }

        Map<String, Object> info = coordinates.get(0);
        double lat = Double.parseDouble(info.get("lat").toString());
        double lon = Double.parseDouble(info.get("lon").toString());

        Map<String, Double> result = new HashMap<>();
        result.put("lat", lat);
        result.put("lon", lon);
        return result;
    }
}
