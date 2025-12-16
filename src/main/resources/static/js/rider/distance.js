// 상품 목록 출력시 매장과 라이더간의 위치 검색
// 정확하지는 않으나 직선거리로 계산
initDistances();

// 단점 : 초기 위치의 값을 가져오는게 느리다
// function updateDistances() {
//     if (navigator.geolocation) {
//         const watchId = navigator.geolocation.watchPosition(
//             // ✅ 성공 콜백
//             function (pos) {
//                 const riderLat = pos.coords.latitude;
//                 const riderLng = pos.coords.longitude;

//                 document.querySelectorAll('.distance').forEach((span) => {
//                     const shopLat = parseFloat(span.dataset.lat);
//                     const shopLng = parseFloat(span.dataset.lng);

//                     if (isNaN(shopLat) || isNaN(shopLng)) {
//                         span.innerText = '위치 오류';
//                         return;
//                     }

//                     const dist = getDistance(riderLat, riderLng, shopLat, shopLng);

//                     if (dist < 1) {
//                         span.innerText = (dist * 1000).toFixed(0) + 'm';
//                     } else {
//                         span.innerText = dist.toFixed(1) + 'km';
//                     }
//                 });
//             },

//             // ✅ 에러 콜백
//             function (err) {
//                 switch (err.code) {
//                     case 1:
//                         console.error('위치 접근 권한 거부');
//                         showToast('error', '위치 권한을 허용해주세요.', 5000);
//                         break;
//                     case 2:
//                         console.error('위치 정보를 가져올 수 없음');
//                         showToast('error', 'GPS 신호를 확인할 수 없습니다.', 5000);
//                         break;
//                     case 3:
//                         console.error('위치 요청 시간 초과');
//                         showToast('error', '위치 요청이 너무 오래 걸립니다.', 5000);
//                         break;
//                     default:
//                         console.error('알 수 없는 오류:', err.message);
//                         showToast('error', '위치 정보를 가져오는 중 오류가 발생했습니다.', 5000);
//                 }
//             },

//             // ✅ 옵션
//             {
//                 enableHighAccuracy: false, // 더 정확한 (GPS 활용)
//                 timeout: 10000, // 10초 안에 못 가져오면 에러
//                 maximumAge: 5000, // 5초 이내 캐시된 위치는 재사용
//             }
//         );

//         return watchId; // 추적 중단할 때 필요
//     } else {
//         alert('이 브라우저는 위치 정보를 지원하지 않습니다.');
//     }
// }

/**
 *  두 좌표 거리 계산 (Haversine 공식)
 *  @param {number} lat1 라이더의 현재 위도
 *  @param {number} lng1 라이더의 현재 경도
 *  @param {number} lat2 매장의 현재 위도
 *  @param {number} lng2 매장의 현재 경도
 *
 *  @return {number} 두 좌표 사이간의 거리 (단위: 1km 이상일 경우 km / 1km 이하일경우 m)
 *
 *  @example
 *  // 결과: 약 500m
 *  getDistance(37.5665, 126.9780, 37.5700, 126.9820);
 *
 *  // 결과: 약 1.5km
 *  getDistance(37.5665, 126.9780, 37.5700, 126.9820);
 *
 */
function getDistance(lat1, lng1, lat2, lng2) {
    const R = 6371; // km 단위
    const dLat = ((lat2 - lat1) * Math.PI) / 180;
    const dLng = ((lng2 - lng1) * Math.PI) / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // km
}

let riderLat; // 라이더의 현재경도

let riderLng; // 라이더의 현재 위도

// ajax에서 거리 처리할때 사용
// 라이더 현재 위치 가져와서 모든 .distance 업데이트
function updateDistances() {
    //navigator.geolocation.getCurrentPosition(function (pos) {
    navigator.geolocation.getCurrentPosition(function (pos) {
        const riderLat = pos.coords.latitude;
        const riderLng = pos.coords.longitude;

        document.querySelectorAll('.distance').forEach((span) => {
            const shopLat = parseFloat(span.dataset.lat);
            const shopLng = parseFloat(span.dataset.lng);

            const dist = getDistance(riderLat, riderLng, shopLat, shopLng);

            if (dist < 1) {
                span.innerText = (dist * 1000).toFixed(0) + 'm';
            } else {
                span.innerText = dist.toFixed(1) + 'km';
            }
        });
    });
}

/**
 * 라이더 위치 추적을 초기화하는 함수
 *
 * - 브라우저에서 Geolocation API 지원 여부 확인
 * - 최초 1회 현재 위치를 가져와 매장과의 거리 계산
 * - 이후에는 watchPosition을 사용하여 실시간 위치 추적 시작
 *
 * @function initDistances
 * @returns {void}
 */
function initDistances() {
    if (!navigator.geolocation) {
        showToast('이 브라우저는 위치 정보를 지원하지 않습니다.');
        return;
    }

    navigator.geolocation.getCurrentPosition(
        function (pos) {
            const lat = pos.coords.latitude;
            const lng = pos.coords.longitude;

            updateDistanceElements(lat, lng);
            startWatch();
        },
        function (err) {
            console.error('초기 위치 가져오기 실패:', err);
            showToast('위치 권한을 허용해주세요!');
        },
        {
            enableHighAccuracy: false,
            timeout: 6000,
            maximumAge: 10000,
        }
    );
}

/**
 * 모든 `.distance` 요소의 텍스트를 라이더와 매장 사이의 거리로 갱신
 *
 * @function updateDistanceElements
 * @param {number} riderLat - 라이더 위도
 * @param {number} riderLng - 라이더 경도
 * @returns {void}
 */
function updateDistanceElements(riderLat, riderLng) {
    document.querySelectorAll('.distance').forEach((span) => {
        const shopLat = parseFloat(span.dataset.lat);
        const shopLng = parseFloat(span.dataset.lng);

        if (isNaN(shopLat) || isNaN(shopLng)) {
            span.innerText = '위치 오류';
            return;
        }

        const dist = getDistance(riderLat, riderLng, shopLat, shopLng);
        span.innerText = dist < 1 ? (dist * 1000).toFixed(0) + 'm' : dist.toFixed(1) + 'km';
    });
}

let watchId;

/**
 * 실시간 위치 추적을 시작하는 함수
 *
 * - navigator.geolocation.watchPosition 사용
 * - 위치가 변경될 때마다 updateDistanceElements 호출
 *
 * @function startWatch
 * @returns {void}
 */
function startWatch() {
    watchId = navigator.geolocation.watchPosition(
        (pos) => {
            const lat = pos.coords.latitude;
            const lng = pos.coords.longitude;
            updateDistanceElements(lat, lng);
        },
        (err) => {
            console.error('실시간 위치 추적 실패:', err);
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 5000,
        }
    );
}

/**
 * 실시간 위치 추적을 중단하는 함수
 *
 * - watchId가 존재하면 clearWatch로 추적 중단
 *
 * @function stopWatch
 * @returns {void}
 */
function stopWatch() {
    if (watchId) {
        navigator.geolocation.clearWatch(watchId);
        console.warn('실시간 추적 중단');
    }
}
