$(document).ready(function () {
    const $scrollBtn = $('.top_scroll_btn'); // "맨 위로" 버튼 선택
    const $contentMain = $('.content_main'); // 스크롤 영역 선택

    // 스크롤 이벤트 감지
    $contentMain.on('scroll', function () {
        // content_main 영역을 200px 이상 스크롤하면 버튼 보이기
        let scrollHeight = $contentMain[0].scrollHeight; // 전체 높이
        let clientHeight = $contentMain[0].clientHeight; // 보이는 높이
        let scrollTop = $contentMain.scrollTop(); // 현재 스크롤 위치
        // console.log('scrollHeight:', scrollHeight);
        // console.log('clientHeight:', clientHeight);
        // console.log('scrollTop:', scrollTop);
        // console.log('최대 스크롤 가능:', scrollHeight - clientHeight);
        if ($contentMain.scrollTop() > 150) {
            $scrollBtn.show(); // 버튼 표시
        } else {
            $scrollBtn.hide(); // 버튼 숨김
        }
    });

    // 버튼 클릭 시 content_main 맨 위로 부드럽게 이동
    $scrollBtn.on('click', function () {
        $contentMain.animate({ scrollTop: 0 }, 0);
        // 400ms 동안 스무스하게 맨 위로 스크롤
    });
});

$(document).ready(function () {
    const $contentMain = $('.content_main'); // 스크롤 영역
    let isLoading = false; // ✅ 요청 중 여부 플래그
    const orderType = $('.content-start').data('order-type');
    let status;
    if (orderType === 'all') {
        status = 2;
    } else if (orderType === 'stay') {
        status = 2;
    } else {
        status = 3;
    }

    $contentMain.on('scroll.infinite', function () {
        let scrollPosition = $contentMain.scrollTop() + $contentMain.innerHeight();
        let scrollHeight = $contentMain[0].scrollHeight;

        if (scrollPosition >= scrollHeight - 10 && !isLoading) {
            isLoading = true; // ✅ 요청 시작

            let orderNoElement = $('.order-card');
            let orderNo = orderNoElement.last().data('order-no');

            $.ajax({
                url: '/rider/moreScroll',
                type: 'POST',
                data: { orderNo, orderType, status },
                success: function (response) {
                    if (response.length === 0) {
                        if ($('.end-text').length === 0) {
                            $('.content-start').append('<div class="end-text">불러올 게시글이 없습니다.</div>');
                        }
                        // ✅ 데이터 없으면 스크롤 이벤트 해제
                        $contentMain.off('scroll.infinite');
                        return;
                    }

                    response.forEach((item) => {
                        $('.content-start').append(renderOrderCard(item, false));
                    });
                    stopWatch();
                    initDistances();
                    //$contentMain.trigger('scroll');
                },
                error: function (xhr, status, message) {
                    console.error('AJAX Error:', message);
                },
                complete: function () {
                    isLoading = false; // ✅ 요청 끝나면 다시 false
                },
            });
        }
    });
});

function formatDateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

function renderOrderCard(item, isFirst) {
    console.log('authNo : ' + authNo);
    return `
    <div class="order-card ${isFirst ? 'active-card' : ''}" data-order-no="${item.orderNo}">
        <!-- 상단 -->
        <div class="order-header">
            <span class="order-no">#${item.orderNo}</span>
            
            ${
                item.status === 2
                    ? `
                <span class="order-time">${formatDateTime(item.orderTime?.requestedAt) || ''}</span>
                <span class="order-status 대기">대기</span>
            `
                    : item.status === 3
                    ? `
                <span class="order-time">${formatDateTime(item.orderTime?.assignedAt) || ''}</span>
                <span class="order-status 수락">수락</span>
            `
                    : item.status === 4
                    ? `
                <span class="order-time">${formatDateTime(item.orderTime?.pickupAt) || ''}</span>
                <span class="order-status 배송중">배송중</span>
            `
                    : item.status === 5
                    ? `
                <span class="order-time">${formatDateTime(item.orderTime?.pickupAt) || ''}</span>
                <span class="order-status 배송완료">배송완료</span>
            `
                    : ''
            }

            <div class="order-price">
                <span class="distance" data-lat="${item.shop.latitude}" data-lng="${item.shop.longitude}">0km</span>
                <span class="fee">${item.deliveryFee.toLocaleString()}원</span>
            </div>
        </div>

        <!-- 중단 -->
        <div class="order-body">
            <div class="d-flex flex-column">
                <div class="payment">선결제</div>
                <div class="details">
                    <span>${item.orderPrice.toLocaleString()}원</span>
                </div>
            </div>

            <div class="d-flex flex-column">
                <div>판매지</div>
                <div class="orderAddress">${item.status === 2 ? item.shop.shopAddress : item.status !== 2 && item.rider.memberNo != authNo ? maskAddress(item.shop.shopAddress) : item.shop.shopAddress}</div>
                </div>
                
                <div class="d-flex flex-column">
                <div>배송지</div>
                <div class="orderAddress">${item.status === 2 ? item.orderAddress : item.status !== 2 && item.rider.memberNo != authNo ? maskAddress(item.orderAddress) : item.orderAddress}</div>
            </div>

             ${item.status === 2 ? `<button class="accept-btn">접수 →</button>` : item.status === 3 && item.rider.memberNo === authNo ? `<button class="shop-btn">매장 위치</button><button type="button" class="pickup-btn">픽업 확인</button>` : item.status === 4 && item.rider.memberNo === authNo ? `<button class="accept-btn">목적지 위치</button>` : `<button class="accept-btn"></button>`}

        
        </div>

        <!-- 하단 -->
        <div class="order-footer"></div>
    </div>`;
}
