// 라이더와 매장에 대한 지도

// 전역 보관
let map = null;
let markers = [];
let lastBounds = null;
let overlays = []; // 커스텀 오버레이 보관

// 경로 캐시 & 렌더 상태
let routeCache = { ready: false, paths: [] }; // paths: kakao.maps.LatLng[] 배열들의 배열
let routeLines = []; // 실제로 지도에 그려진 Polyline들

const dimmed = $('.dimmed');
const detailModal = $('#detail-modal');
const pickupModal = $('#pickup-modal');
const completeModal = $('#complete-modal');
const previewModal = $('#preview-modal');
const previewCloseBtn = $('.preview-close-btn');
const closeTopBtn = $('.close_modal').children();
const closeBottomBtn = $('.close-btn');

previewCloseBtn.on('click', function () {
    previewModal.hide();
});

function closeDeliveryModal() {
    dimmed.hide();
    detailModal.hide();
    pickupModal.hide();
    completeModal.hide();
}

closeBottomBtn.add(closeTopBtn).on('click', function () {
    dimmed.hide();
    detailModal.hide();
    pickupModal.hide();
    completeModal.hide();
});

// 모달 배경 클릭 시 닫기
dimmed.on('click', function () {
    closeDeliveryModal();
});

// 모달 ESC 키 닫기
$(document).on('keydown', function (e) {
    if (e.key === 'Escape') {
        closeDeliveryModal();
    }
});

/** 현재 위치를 Promise로 래핑 */
function getCurrentPosition(opts = { enableHighAccuracy: false, timeout: 5000, maximumAge: 10000 }) {
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition((pos) => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }), reject, opts);
    });
}

/** 마커 제거 */
function clearMarkers() {
    markers.forEach((m) => m.setMap(null));
    markers.length = 0;
}

/** 오버레이 제거 */
function clearOverlays() {
    overlays.forEach((o) => o.setMap(null));
    overlays.length = 0;
}

/** 경로 제거 */
function clearRoutes() {
    routeLines.forEach((r) => r.setMap(null));
    routeLines.length = 0;
}

/**
 * Kakao Mobility directions sections -> Polyline path로 변환
 * @param {Array} sections sections[0].roads[*].vertexes (lng,lat 반복)
 * @return {kakao.maps.LatLng[]} 하나의 경로 path
 */
function makePathFromSections(sections) {
    const section = sections && sections[0];
    if (!section) return [];
    const path = [];
    section.roads.forEach((road) => {
        const v = road.vertexes; // [lng,lat,lng,lat,...]
        for (let i = 0; i < v.length; i += 2) {
            const lng = v[i],
                lat = v[i + 1];
            path.push(new kakao.maps.LatLng(lat, lng)); // (lat, lng) 순서 주의
        }
    });
    return path;
}

var geocoder = new kakao.maps.services.Geocoder();

function getLoc(address) {
    return new Promise((resolve, reject) => {
        geocoder.addressSearch(address, function (result, status) {
            if (status === kakao.maps.services.Status.OK) {
                resolve({ x: result[0].x, y: result[0].y });
            } else {
                reject('주소 검색 실패');
            }
        });
    });
}

/** 지도 생성 또는 갱신 */
async function initOrUpdateMap(destinationLat, destinationLng) {
    const mapContainer = document.getElementById('map');
    if (!mapContainer) {
        console.warn('#map 컨테이너가 없습니다.');
        return;
    }

    // 컨테이너가 보이는 상태에서 실행되어야 함 (모달 show 이후)
    if (!map) {
        map = new kakao.maps.Map(mapContainer, {
            center: new kakao.maps.LatLng(destinationLat, destinationLng),
            level: 4,
        });
    } else {
        // 두 번째 이상 열림: 레이아웃 먼저 재계산
        map.relayout();
    }
    // 위치 얻기 (실패해도 가게만이라도 표시)
    // 라이더의 위치가 잡히지 않을 경우 (한자리에 오래 있을 경우 rider 가 null임)
    // 수정 setItem을 사용해서 실패시 캐시 좌표로  Fallback
    try {
        rider = await getCurrentPosition();
        sessionStorage.setItem('lastRiderPos', JSON.stringify(rider));
    } catch (e) {
        console.warn('현재 위치 획득 실패:', e);
        const cached = sessionStorage.getItem('lastRiderPos');
        if (cached) {
            rider = JSON.parse(cached); // { lat, lng }
        }
    }

    $.ajax({
        url: '/rider/ajaxGetKakaoMobilityDirections',
        type: 'POST',
        data: { riderLat: rider.lat, riderLng: rider.lng, destinationLat, destinationLng },
        success: function (response) {
            const data = typeof response === 'string' ? JSON.parse(response) : response;

            // 1) route0 안전하게 찾기 (data.raw.routes[0]까지 커버)
            const route0 = (data.routes && data.routes[0]) || (data.raw && data.raw.routes && data.raw.routes[0]) || null;

            if (!route0) {
                console.warn('routes[0]가 없습니다:', data);
                // 버튼 상태 갱신
                const btn = document.getElementById('drawRouteBtn');
                if (btn) {
                    btn.disabled = true;
                    btn.textContent = '경로 없음';
                }
                return;
            }

            // (선택) 카카오 result_code 검사
            if (typeof route0.result_code !== 'undefined' && route0.result_code !== 0) {
                console.warn('Kakao directions 실패:', route0.result_code, route0.result_msg);
                const btn = document.getElementById('drawRouteBtn');
                if (btn) {
                    btn.disabled = true;
                    btn.textContent = '경로 없음';
                }
                return;
            }

            // 2) sections/summary 추출 (세 가지 케이스 커버)
            const sections = data.sections || route0.sections || null;

            const summary = data.summary || route0.summary || null;

            // 3) Polyline path 만들기
            const path = makePathFromSections(sections);

            // 4) 캐시에 저장(그리지는 않음)
            routeCache.paths = path.length ? [path] : [];
            routeCache.ready = routeCache.paths.length > 0;

            // 5) 버튼 라벨/활성화
            const btn = document.getElementById('drawRouteBtn');
            if (btn) {
                if (routeCache.ready) {
                    // 거리/시간 라벨 추가 (있을 때만)
                    const dist = summary?.distance ?? data.distance; // m
                    const dur = summary?.duration ?? (data.durationMin ? data.durationMin * 60 : null); // sec
                    const fmtKm = (m) => (m >= 1000 ? (m / 1000).toFixed(1) + 'km' : Math.round(m) + 'm');
                    const fmtMin = (s) => (s >= 60 ? Math.round(s / 60) + '분' : (s ?? 0) + '초');

                    btn.disabled = false;
                    if (dist != null && dur != null) {
                        btn.textContent = `경로 그리기 (${fmtKm(dist)} · ${fmtMin(dur)})`;
                    } else {
                        btn.textContent = '경로 그리기';
                    }
                } else {
                    btn.disabled = true;
                    btn.textContent = '경로 없음';
                }
            }
        },
        error: function (xhr, status, message) {},
    });

    // 마커 갱신 , 기존 오버레이 제거
    clearMarkers();
    clearOverlays();
    clearRoutes();

    const positions = [{ title: '매장', latlng: new kakao.maps.LatLng(destinationLat, destinationLng), img: '/image/rider/shopIcon.png' }];

    if (rider) {
        positions.push({ title: '라이더', latlng: new kakao.maps.LatLng(rider.lat, rider.lng), img: '/image/rider/riderIcon.png' });
    }

    const bounds = new kakao.maps.LatLngBounds();
    positions.forEach((pos) => {
        const markerImage = new kakao.maps.MarkerImage(pos.img, new kakao.maps.Size(48, 52), { offset: new kakao.maps.Point(24, 52) });
        const marker = new kakao.maps.Marker({ map, position: pos.latlng, title: pos.title, image: markerImage });
        markers.push(marker);
        bounds.extend(pos.latlng);
        // ★ 커스텀 오버레이는 "마커 만든 직후"에 생성/추가
        const html = `<div style="
      background:#ff6b35;border:1px solid #e5e7eb;border-radius:8px;
      padding:6px 10px;font-size:12px;line-height:1;white-space:nowrap;
      box-shadow:0 2px 8px rgba(0,0,0,.15);
    ">${pos.title}</div>`;

        const overlay = new kakao.maps.CustomOverlay({
            position: pos.latlng,
            content: html,
            xAnchor: 0.5,
            yAnchor: 2.8, // 마커 위로 살짝
            zIndex: 3,
        });
        overlay.setMap(map);
        overlays.push(overlay);
    });

    // 중요: 레이아웃 이후 범위/센터 재적용
    map.relayout();
    if (positions.length > 1) {
        map.setBounds(bounds);
        lastBounds = bounds;
    } else {
        map.setCenter(new kakao.maps.LatLng(destinationLat, destinationLng));
        lastBounds = null;
    }

    // 불필요한 상호작용 비활성화 (선택)
    map.setDraggable(false);
    map.setZoomable(false);
}

/** 캐시에 있는 경로를 현재 map에 그리기 */
function drawCachedRoutes() {
    if (!map) return;
    if (!routeCache.ready || !routeCache.paths.length) {
        alert('아직 경로 데이터가 준비되지 않았습니다.');
        return;
    }
    // 이미 그려져 있으면 다시 그리지 않음
    if (routeLines.length) return;

    routeCache.paths.forEach((path) => {
        const line = new kakao.maps.Polyline({
            map,
            path,
            strokeWeight: 5,
            strokeColor: '#ef4444',
            strokeOpacity: 0.9,
        });
        routeLines.push(line);
    });

    // 화면을 경로 전체로 맞춤
    const bounds = new kakao.maps.LatLngBounds();
    routeCache.paths.forEach((p) => p.forEach((pt) => bounds.extend(pt)));
    map.setBounds(bounds);
}

/** 경로 그리기 버튼 */
$(document).on('click', '#drawRouteBtn', function () {
    let text = $('#drawRouteBtn').text();
    console.log(text);
    if (text.includes('경로 그리기')) {
        drawCachedRoutes();
        $(this).text('네비게이션 출력');
    } else if (text.includes('경로 검색중')) {
        showToast('경로 검색중 입니다.');
    } else if (text.includes('네비게이션 출력')) {
        openKakaoRoute();
        closeDeliveryModal();
    }
});

/**
 * Kakao Map 길찾기 새 창 오픈
 * @param {{name:string, lat:number, lng:number}} from  출발지
 * @param {{name:string, lat:number, lng:number}} to    목적지
 * @param {'car'|'traffic'|'walk'|'bicycle'} mode       이동수단 (기본 car)
 */
function openKakaoRoute() {
    const enc = encodeURIComponent;
    const type = $('#drawRouteBtn').data('fromTo');
    let rider;
    const cached = sessionStorage.getItem('lastRiderPos');
    if (cached) {
        rider = JSON.parse(cached); // { lat, lng }
    }
    let destinationLat = $('.modal_box').data('destinationLat');
    let destinationLng = $('.modal_box').data('destinationLng');
    let segTo;
    if (type === 'riderToShop') {
        segTo = `매장,${destinationLat},${destinationLng}`;
    } else {
        segTo = `배송지,${destinationLat},${destinationLng}`;
    }
    const segFrom = `라이더,${rider.lat},${rider.lng}`;

    // 방법 A: from/to 패턴
    // const url = `https://map.kakao.com/link/from/${segFrom}/to/${segTo}`;

    // 방법 B: 이동수단 지정(by/{mode})

    // 모바일 여부 감지
    const isMobile = /Android|iPhone|iPad|iPod/i.test(navigator.userAgent);

    // 분기 처리
    let url;
    if (isMobile) {
        url = `kakaomap://route?sp=${from.lat},${from.lng}&ep=${to.lat},${to.lng}&by=car`;
    } else {
        url = `https://map.kakao.com/link/by/car/${segFrom}/${segTo}`;
    }

    window.open(url, 'kakaoRoute', 'width=1200,height=900,noopener,noreferrer');
}

/** 모달 오픈 → 레이아웃이 잡힌 "다음 프레임"에 지도 초기화 */
$(document).on('click', '.shop-btn', function () {
    dimmed.show();
    detailModal.show();
    const $card = $(this).closest('.order-card');
    const lat = parseFloat($card.find('.distance').data('lat'));
    const lng = parseFloat($card.find('.distance').data('lng'));
    $('#drawRouteBtn').data('fromTo', 'riderToShop');
    $('.modal_box').data('destinationLat', lat);
    $('.modal_box').data('destinationLng', lng);
    $('#drawRouteBtn').text('경로 검색중..');
    // **핵심**: 실제로 보이는 다음 렌더 사이클에 실행 (두 번 감싸면 더 안전)
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            initOrUpdateMap(lat, lng);
        });
    });
});

/** 모달이 다시 보여질 때 강제 리레이아웃 (모달 컴포넌트에 맞춰 바꿔도 됨) */
$(document).on('detailModal:shown', function () {
    if (map) {
        requestAnimationFrame(() => {
            map.relayout();
            if (lastBounds) map.setBounds(lastBounds);
        });
    }
});

/** 모달을 단순히 display:none ↔ block 토글한다면, show 직후에 강제 호출 */
function onModalShow() {
    if (map) {
        requestAnimationFrame(() => {
            map.relayout();
            if (lastBounds) map.setBounds(lastBounds);
        });
    }
}
// 예: detailModal.show() 구현 내부/직후에 onModalShow() 호출
