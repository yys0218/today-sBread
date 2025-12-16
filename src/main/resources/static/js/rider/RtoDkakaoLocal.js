// 라이더와 배달에 관한 지도
$(document).on('click', '.delivery-btn', async function () {
    console.log('클릭했습니다.');
    dimmed.show();
    detailModal.show();
    console.log('클릭했습니다.');
    const orderAddress = $(this).closest('.order-card').find('.orderAddress').eq(1).text();

    let destinationLat;
    let destinationLng;
    try {
        const loc = await getLoc(orderAddress);
        destinationLat = loc.y;
        destinationLng = loc.x;
    } catch (err) {
        console.error(err);
    }

    console.log(destinationLat);
    console.log(destinationLng);

    $('.modal_box').data('destinationLat', destinationLat);
    $('.modal_box').data('destinationLng', destinationLng);
    $('#drawRouteBtn').data('fromTo', 'riderToDest');
    $('#drawRouteBtn').text('경로 검색중..');
    // **핵심**: 실제로 보이는 다음 렌더 사이클에 실행 (두 번 감싸면 더 안전)
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            RtoDinitOrUpdateMap(destinationLat, destinationLng);
        });
    });
});

/** 지도 생성 또는 갱신 */
async function RtoDinitOrUpdateMap(destinationLat, destinationLng) {
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
    const positions = [{ title: '배송지', latlng: new kakao.maps.LatLng(destinationLat, destinationLng), img: '/image/rider/destinationIcon.png' }];
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
