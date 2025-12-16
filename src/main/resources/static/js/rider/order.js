/**
 * 카드 클릭 시 해당 주문의 상세 정보를 Ajax로 불러와서 오른쪽 상세 영역에 표시한다.
 *
 * 기능:
 * 1. 모든 카드의 active 상태를 제거하고 클릭한 카드에만 active 추가
 * 2. 클릭된 카드의 data-order-no 값을 이용해 서버에 Ajax 요청
 * 3. 응답 데이터(가격, 전화번호, 주소, 요청사항, 상품 내역)를 상세 영역에 반영
 */

function timeFormat(date) {
    return new Date(date).toLocaleString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    });
}

function timeSet(response) {
    $('.step').eq(1).find('.time').text('');
    $('.step').eq(2).find('.time').text('');
    $('.step').eq(3).find('.time').text('');
    $('.step').eq(0).addClass('done');
    $('.step').eq(0).find('.time').text(timeFormat(response.orderTime.requestedAt));
    if (response.status == 2) {
        $('.step').eq(1).addClass('active');
    } else if (response.status == 3) {
        $('.step').eq(1).addClass('done');
        $('.step').eq(2).addClass('active');
        $('.step').eq(1).find('.time').text(timeFormat(response.orderTime.assignedAt));
    } else if (response.status == 4) {
        $('.step').eq(1).addClass('done');
        $('.step').eq(2).addClass('done');
        $('.step').eq(3).addClass('active');
        $('.step').eq(1).find('.time').text(timeFormat(response.orderTime.assignedAt));
        $('.step').eq(2).find('.time').text(timeFormat(response.orderTime.pickupAt));
    } else if (response.status == 5) {
        $('.step').eq(1).addClass('done');
        $('.step').eq(2).addClass('done');
        $('.step').eq(3).addClass('done');
        $('.step').eq(1).find('.time').text(timeFormat(response.orderTime.assignedAt));
        $('.step').eq(2).find('.time').text(timeFormat(response.orderTime.pickupAt));
        $('.step').eq(3).find('.time').text(timeFormat(response.orderTime.completedAt));
    }
}

$(document).on('click', '.order-card', function () {
    // 모든 카드에서 active 제거
    $('.order-card').removeClass('active-card');
    // 클릭된 카드만 active 추가
    $(this).addClass('active-card');

    // 배송 정보 상태 알려주는 곳의 done , active 클래스 제거
    $('.step').removeClass('done active');

    // 클릭된 카드의 data 속성의 orderNo 가지고 ajax로 상세 정보 조회
    let orderNo = $(this).data('order-no');
    console.log(orderNo);
    $.ajax({
        url: '/rider/ajaxOrderDetail',
        type: 'POST',
        data: { orderNo },
        success: function (response) {
            console.log(response);
            // 가격 요소
            let priceElement = $('#orderPrice');
            // 전화번호 요소
            let phoneElement = $('#orderPhone');
            // 배송지 주소 요소
            let deliveryAddressElement = $('#delivery_address');
            // 고객의 배송 메모 요소
            let orderRequestElement = $('.order-request');
            orderRequestElement.empty();
            // 상품 주문 내역 요소
            let orderDetailElement = $('.order-details');
            orderDetailElement.empty();

            let status = response.status;

            console.log(response);

            // 데이터 반영
            priceElement.val(response.orderPrice.toLocaleString() + '원');
            console.log(response.orderPhone);
            phoneElement.val(response.orderPhone);
            deliveryAddressElement.text(response.orderAddress);
            timeSet(response);
            response.orderRequest !== null ? orderRequestElement.append('<p>' + response.orderRequest + '</p>') : orderRequestElement.append('요청사항이 없습니다.');

            // 상품 내역 반복 출력
            for (let i = 0; i < response.orderDetail.length; i++) {
                let productName = response.orderDetail[i].product.productName;
                let quantity = response.orderDetail[i].quantity;
                let price = response.orderDetail[i].product.price;
                let totalPrice = price * quantity;

                orderDetailElement.append(`<p> ${productName} ${quantity}개 ${totalPrice.toLocaleString()}원</p>`);
            }
        },
        error: function (xhr, status, message) {},
    });
});

/**
 * 접수 버튼 클릭 시 해당 주문을 배차 요청하는 Ajax 호출 처리
 *
 * 기능:
 * 1. 클릭된 버튼과 가까운 order-card에서 orderNo 추출
 * 2. 버튼 비활성화 처리
 * 3. 가게 주소, 배송지 주소를 마스킹 처리 후 화면에 반영
 * 4. Ajax 요청을 통해 배차 요청 처리 → 서버 응답에 따라 알림창 출력
 */
$(document).on('click', '.accept-btn', async function () {
    let orderNo = $(this).closest('.order-card').data('order-no');

    // // 버튼 비활성화
    const $this = $(this);

    // 가게, 배송지 주소 가져오기
    let shopAddress = $(this).closest('.order-card').find('.orderAddress').eq(0).text();
    let orderAddress = $(this).closest('.order-card').find('.orderAddress').eq(1).text();

    console.log('shop : ' + shopAddress);
    console.log('order : ' + orderAddress);

    // 각 주소 마스킹 처리
    let maskShopAddr = maskAddress(shopAddress);
    let maskOrderAddr = maskAddress(orderAddress);

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

    let destinationLat;
    let destinationLng;
    try {
        const loc = await getLoc(shopAddress);
        destinationLat = loc.y;
        destinationLng = loc.x;
    } catch (err) {
        console.error(err);
    }

    $.ajax({
        url: '/rider/ajaxGetKakaoMobilityDirections',
        type: 'POST',
        data: { riderLat: rider.lat, riderLng: rider.lng, destinationLat, destinationLng },
        success: function (response) {
            const data = typeof response === 'string' ? JSON.parse(response) : response;
            if (response) {
                $.ajax({
                    url: '/rider/ajaxOrderAccept',
                    type: 'POST',
                    data: { orderNo, durationMin: data.durationMin },
                    success: function (response) {
                        if (response === 'success') {
                            $this.removeClass('accept-btn');
                            $this.addClass('detail-btn');

                            showSuccessTitleAlert('성공', '배차요청에 성공했습니다.');
                            window.location.reload();
                            webSocket.send(
                                JSON.stringify({
                                    handlerType: 'riderAssignHandler',
                                    orderNo: orderNo,
                                })
                            );
                            location.reload();
                        } else if (response === 'invalid access') {
                            showErrorTitleAlert('잘못된 접근', '해당 주문이 삭제되었거나 만료되어 확인할 수 없습니다.');
                        } else if (response === 'already in use') {
                            showErrorTitleAlert('잘못된 접근', '이미 다른 라이더에게 배정된 주문입니다.');
                        }
                    },
                    error: function (xhr, status, message) {
                        console.error('배차 요청 실패:', message);
                    },
                });
            }
        },
    });
});

/**
 * 주소 문자열을 마스킹 처리하는 함수
 *
 * 예시:
 * "서울특별시 관악구 남부순환로 1820 6층" → "서울특별시 관악구 남부순환로 ****"
 *
 * @param {string} address - 원본 주소 문자열
 * @returns {string} 마스킹 처리된 주소
 *
 * @example
 * "서울특별시 관악구 남부순환로 1820 6층" → "서울특별시 관악구 남부순환로 ****"
 *
 */
function maskAddress(address) {
    return address.replace(/(\d+\s*)(.*)/, (match, road, detail) => {
        return road + '****';
    });
}

$(document).on('click', '.pickup-btn', function (e) {
    const orderNo = $(this).closest('[data-order-no]').data('order-no');
    console.log(orderNo);
    pickupModal.show();

    //  주문 번호
    const merchantUid = pickupModal.find('#dModal-merchantUid');
    // 가게 이름
    const shopName = pickupModal.find('#dModal-shopName');
    // 가게 번호
    const shopPhone = pickupModal.find('#dModal-shopPhone');
    // 배달 주소
    const address = pickupModal.find('#dModal-address');
    // 배달 연락처
    const phone = pickupModal.find('#dModal-phone');
    // 고객 요청 사항
    const orderRequest = pickupModal.find('#dModal-orderRequest');
    // 상품 수량
    const productCount = pickupModal.find('#dModal-product-count');
    // 주문 내역
    const productList = pickupModal.find('#dModal-product-list');
    productList.empty();
    const pickupAcceptBtn = pickupModal.find('#pickup-accept-btn');
    let pElement = $('<p></p>');
    let totalQuantity = 0;
    $.ajax({
        url: '/rider/ajaxOrderDetail',
        type: 'POST',
        data: { orderNo },
        success: function (response) {
            pickupModal.attr('data-order-no', orderNo);
            merchantUid.text(response.merchantUid);
            shopName.text(response.shop.shopName);
            shopPhone.text(response.shop.businessContact);
            address.text(response.orderAddress);
            phone.text(response.orderPhone);
            orderRequest.empty().append(response.orderRequest !== null ? pElement.text(response.orderRequest) : '요청사항이 없습니다.');
            response.orderDetail.forEach((p) => {
                totalQuantity += p.quantity;
                let totalPrice = p.price * p.quantity;
                productList.append(`<p> ${p.product.productName} ${p.quantity}개 ${totalPrice.toLocaleString()}원</p>`);
            });
            productCount.text(totalQuantity);
        },
        error: function (xhr, status, message) {},
    });
});

// 픽업 확인 버튼 을 눌렀을 때
$('#pickup-accept-btn').on('click', async function () {
    const orderNo = $(this).closest('[data-order-no]').data('order-no');
    console.log(orderNo);
    const merchantUid = $('#dModal-merchantUid').text();
    const address = $('#dModal-address').text();
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
    console.log('rider : ' + rider);
    let destinationLat;
    let destinationLng;
    try {
        const loc = await getLoc(address);
        destinationLat = loc.y;
        destinationLng = loc.x;
    } catch (err) {
        console.error(err);
    }

    showConfirmTitleAlert(
        (title = '상품 픽업 확인'),
        (msg = '상품번호 : ' + merchantUid + '<br>상품을 픽업하셨습니까?'),
        () => {
            // 사용자가 '예'를 눌렀을 때 실행
            console.log('확인 클릭됨');

            $.ajax({
                url: '/rider/ajaxGetKakaoMobilityDirections',
                type: 'POST',
                data: { riderLat: rider.lat, riderLng: rider.lng, destinationLat, destinationLng },
                success: function (response) {
                    console.log('response :  ' + response);
                    const data = typeof response === 'string' ? JSON.parse(response) : response;
                    $.ajax({
                        url: '/rider/ajaxPickupAccept',
                        type: 'POST',
                        data: { orderNo, durationMin: data.durationMin },
                        success: function (response) {
                            if (response == 'success') {
                                // websocket
                                webSocket.send(
                                    JSON.stringify({
                                        handlerType: 'riderPickupHandler',
                                        fromMemberNo: authNo,
                                        orderNo: orderNo,
                                    })
                                );
                                closeDeliveryModal();
                                window.location.reload();
                                showTitleToast('픽업성공', '안전 운행 하세요.', 'success');
                            } else if (response === 'fail') {
                                showTitleToast('픽업실패', '다시 시도해 주세요.', 'error');
                            } else if (response === 'orderNotFound') {
                                showTitleToast('픽업실패', '상품을 찾을 수 없습니다.', 'error');
                            } else if (response === 'statusNot3') {
                                showTitleToast('픽업실패', '회원님이 수락한 배달이 아닙니다.', 'error');
                            }
                        },
                        error: function (xhr, status, message) {},
                    });
                },
                error: function (xhr, status, message) {},
            });
        },
        () => {
            // 사용자가 '아니오'를 눌렀을 때 실행
            // closeDeliveryModal();
            setTimeout(() => {
                showTitleToast('취소', '픽업 후 다시 요청해주세요.', 'error');
            }, 100); // 0.1초 뒤 실행
        }
    );
});

$(document).on('click', '#complete-accept-btn', function () {
    const orderNo = $(this).closest('[data-order-no]').data('order-no');
    const file = $('#completeFile');
    console.log('orderNo : ' + orderNo);
    console.log('file : ' + file);
    if (file.val() !== '') {
        const formData = new FormData();
        formData.append('orderNo', orderNo);
        formData.append('file', file[0].files[0]);
        $.ajax({
            url: '/rider/ajaxCompleteAccept',
            type: 'POST',
            data: formData,
            processData: false, // 반드시 false
            contentType: false, // 반드시 false
            success: function (response) {
                if (response === 'success') {
                    closeDeliveryModal();
                    webSocket.send(
                        JSON.stringify({
                            handlerType: 'riderDeliveryHandler',
                            fromMemberNo: authNo,
                            orderNo: orderNo,
                        })
                    );
                    showTitleToast('배송완료', '배송이 완료되었습니다.', 'success');
                    location.reload();
                } else if (response === 'fail') {
                    showTitleToast('배송실패', '다시 시도해 주세요.', 'error');
                } else if (response === 'orderNotFound') {
                    showTitleToast('배송실패', '상품을 찾을 수 없습니다.', 'error');
                }
            },
            error: function (xhr, status, error) {
                console.error('업로드 실패:', error);
                showTitleToast('실패', '업로드 중 오류가 발생했습니다.', 'error');
            },
        });
    } else {
        showTitleToast('실패', '사진 파일을 올려주세요.', 'error');
    }
});

$(document).on('click', '.complete-btn', function () {
    $('#completeFile').val('');
    const orderNo = $(this).closest('[data-order-no]').data('order-no');
    console.log('클릭');
    completeModal.show();

    //  주문 번호
    const merchantUid = completeModal.find('#dModal-merchantUid');
    // 가게 이름
    const shopName = completeModal.find('#dModal-shopName');
    // 가게 번호
    const shopPhone = completeModal.find('#dModal-shopPhone');
    // 배달 주소
    const address = completeModal.find('#dModal-address');
    // 배달 연락처
    const phone = completeModal.find('#dModal-phone');
    // 고객 요청 사항
    const orderRequest = completeModal.find('#dModal-orderRequest');
    // 상품 수량
    const productCount = completeModal.find('#dModal-product-count');
    // 주문 내역
    const productList = completeModal.find('#dModal-product-list');
    productList.empty();

    let pElement = $('<p></p>');
    let totalQuantity = 0;
    $.ajax({
        url: '/rider/ajaxOrderDetail',
        type: 'POST',
        data: { orderNo },
        success: function (response) {
            completeModal.attr('data-order-no', orderNo);
            merchantUid.text(response.merchantUid);
            shopName.text(response.shop.shopName);
            shopPhone.text(response.shop.businessContact);
            address.text(response.orderAddress);
            phone.text(response.orderPhone);
            orderRequest.empty().append(response.orderRequest !== null ? pElement.text(response.orderRequest) : '요청사항이 없습니다.');
            response.orderDetail.forEach((p) => {
                totalQuantity += p.quantity;
                let totalPrice = p.price * p.quantity;
                productList.append(`<p> ${p.product.productName} ${p.quantity}개 ${totalPrice.toLocaleString()}원</p>`);
            });
            productCount.text(totalQuantity);
        },
        error: function (xhr, status, message) {},
    });
});

$(document).on('click', '.preview', function () {
    let attr = $('#preview').attr('src');
    if (attr) {
        previewModal.show();
    }
});

$(document).on('change', '#completeFile', function () {
    const file = event.target.files[0]; // 선택된 파일 (첫 번째)
    if (file) {
        const reader = new FileReader();
        reader.onload = function (e) {
            $('#preview').attr('src', e.target.result); // base64 데이터 URL
        };
        reader.readAsDataURL(file);
    }
    let attr = $('#preview').attr('src');
    if (attr) {
        $('.preview').hide();
    } else {
        $('.preview').show();
    }
});

$(document).on('click', '#deliveryFeeSettle', function () {
    $.ajax({
        url: '/center/settle/apply',
        type: 'post',
        data: { memberNo },
        success: function (response) {
            switch (response) {
                case 'zero':
                    // 잔액이 없을때
                    showErrorAlert('보유중인 잔액이 없습니다.');
                    break;
                case 'success':
                    showSuccessAlert('정산 신청에 성공하였습니다.');
                    $.ajax({
                        url: '/rider/authenticatoinUpdate',
                        type: 'post',
                        data: { memberNo },
                        success: function (response) {
                            switch (response) {
                                case 'success':
                                    window.location.reload();
                                    break;
                                default:
                                    showErrorTitleAlert('정보 업데이트 실패', '새로고침을 진행합니다.');
                                    window.location.reload();
                                    break;
                            }
                        },
                        error: function (xhr, status, error) {},
                    });
                    break;
                case 'error':
                    // DTO 못찾을때
                    showErrorAlert('잠시후 다시 시도해주세요.');
                    break;
                default:
                    showErrorAlert('잠시후 다시 시도해주세요.');
                    break;
            }
        },
        error: function (xhr, status, message) {},
    });
});
