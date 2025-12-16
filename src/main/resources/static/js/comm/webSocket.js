//1. 웹소켓 객체 생성
const webSocket = new WebSocket('ws://localhost:8080/ws/alert');

// 로그인 폼에서 submit 이벤트 감지
const loginForm = document.querySelector('#loginForm');
if (loginForm) {
    loginForm.addEventListener(
        'submit',
        () => {
            const sound = document.getElementById('alertSound');
            if (!sound) return;

            sound
                .play()
                .then(() => {
                    sound.pause();
                    sound.currentTime = 0;
                    console.log('🔊 알림 사운드 권한 확보됨 (form submit)');
                })
                .catch((err) => console.warn('권한 확보 실패:', err));
        },
        { once: true } // 첫 로그인 시점에서만 실행
    );
}

/**
 * 사운드 알림
 * header.html 파일안에 있는 <Audio> 실행
 * .play() 메서드는 DOM 객체의 메서드 (jQuery 사용 불가)
 */
function shopSoundAlert() {
    const sound = document.getElementById('alertSound');
    if (sound) {
        sound.play().catch((err) => console.warn('사운드 재생 실패:', err));
    }
}
/**
 * 웹소켓 핸들러 모음
 * - 서버에서 전달된 handlerType 에 따라 각각 실행됨
 * - shopSoundAlert() 실행 → 알림을 받는 사용자 뷰에서 알림 사운드 재생
 */
const handlers = {
    alertHandler: (data) => {
        console.log(data);
        shopSoundAlert();
    },

    /** 
    라이더 배송 수락시 알림처리
    - handlerType: 'riderAssignHandler';
    - memberType: 'member'; (member:구매자에게 알림 / shop:매장에 알림)
    - orderNo : (주문 번호)
    */
    riderAssignHandler: (data) => {
        console.log(data);
        shopSoundAlert();
        if (data.memberType === 'member') {
            // 구매자 알림
            showSuccessTitleAlert('배송 수락', '라이더가 배송을 수락했습니다.')
        } else {
            // 판매자 알림
            showSuccessTitleAlert('배송 수락', '라이더가 배송을 수락했습니다.');
        }
    },


    /**
     * 라이더 위치 요청시 받은 결과
     * - data.handlerType : 핸들러명
     * - data.lat : 라이더의 경도 좌표
     * - data.lng : 라이더의 위도 좌표
     * - data.riderMemberNo : 위치를 보낸 라이더 번호
     * - data.buyerMemberNo : 라이더 위치를 받을 구매자 번호
     */
    riderLocResponseHandler: (data) => {
        console.log(data);
    },

    /**
     *라이더 픽업시 알림처리 (매장엔 알림X 회원에게만 알림)
     *- handlerType: 'riderPickupHandler';
     *- orderNo : (주문 번호)
     * 알림에 상품명 띄워주고 싶을시 ajax로 상품 조회후 -> 알림
     */
    riderPickupHandler: (data) => {
        console.log(data);
        shopSoundAlert();
        showSuccessTitleAlert('픽업', '라이더가 해당 상품을 픽업했습니다.');
    },

    /**  
    라이더 배송 완료시 알림처리
    - handlerType: 'riderDeliveryHandler';
    - memberType: 'member'; (member:구매자에게 알림 / shop:매장에 알림)
    - orderNo : (주문 번호)
    */
    riderDeliveryHandler: (data) => {
        console.log(data);
        if (data.memberType === 'member') {
            // 구매자 알림
            showSuccessTitleAlert('배송 완료', '라이더가 배송을 완료했습니다.');
        } else {
            // 매장에 알림
            showSuccessTitleAlert('배송 완료', '라이더가 배송을 완료했습니다.');
        }
    },
};

// 웹소켓 받는 부분
webSocket.addEventListener('message', (msg) => {
    const data = JSON.parse(msg.data);
	
    const handler = handlers[data.handlerType];
    if (handler) {
        handler(data);
    } else {
        console.warn('알 수 없는 handlerType:', data.handlerType);
    }
}); 

/**
 * 웹소켓 메시지를 간편하게 전송하는 함수
 * 지정된 핸들러 타입과 발신자/주문 번호를 JSON 문자열로 변환하여
 * 서버(WebSocket)로 전송합니다.
 *
 * @param {string} handlerType  핸들러명 (예: riderPickupHandler)
 * @param {number} fromMemberNo 보내는 사용자 고유번호
 * @param {number} orderNo   orderHistory 고유 번호
 *
 * @example
 * 예시)라이더가 픽업 완료 알림을 보낼 때
 * webSocketSend("riderPickupHandler", 101, 5001);
 */
function riderWebSocketSend(handlerType, fromMemberNo, orderNo) {
    webSocket.send(
        JSON.stringify({
            handlerType,
            fromMemberNo,
            orderNo,
        })
    );
}

//2. 로그인한 사용자인지 확인 copyHandler파일을 복사해서 사용할 handler 생성 ( /websocket/handler/클래스 추가)
// 로그인 했을 때 보낼 webSocket
if (memberNo !== 0) {
    webSocket.onopen = (event) => {
        console.log('실행');
        webSocket.send(
            JSON.stringify({
                handlerType: 'loginHandler', //행동유형?마다  handler있어야됨
                fromMemberNo: memberNo, //보내는 값?
            })
        );
    };
}

webSocket.onclose = (event) => {
    console.log('WS Closed:', event.code, event.reason);
};
