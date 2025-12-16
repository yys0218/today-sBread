const webSocket = new WebSocket('ws://localhost:8080/ws/alert');

// 1. 웹소켓 연결 : 서버에 WebSocket 접속
// onopen 이벤트 : 채팅 접속시1회 실행되는 이벤트
if (authNo != 0) {
    webSocket.onopen = (event) => {
        // .send -> ChatHandelr클래스의 handleMessage 메서드 로 사용자의 이름과 메세지 전달
        // JSON.stringify({}) : JSON 객체를 문자열로 변환
        webSocket.send(
            JSON.stringify({
                handlerType: 'riderLoginHandler',
                fromMemberNo: authNo,
            })
        );
    };
}

const riderHandlers = {
    // 구매자 -> 라이더한테 좌표 요청
    riderLocRequestHandler: async (data) => {
        console.log('riderLocRequestHandler 실행');
        // 구매자 번호
        const buyerMemberNo = data.buyerMemberNo;
        // 라이더 번호
        const riderMemberNo = data.riderMemberNo;
        // 해당 라이더의 좌표 가져오기
        // rider = {lat:latitude , lng:longitude}
        try {
            rider = await getCurrentPosition();
            sessionStorage.setItem('lastRiderPos', JSON.stringify(rider));
        } catch (e) {
            console.warn('현재 위치 획득 실패:', e);
            const cached = sessionStorage.getItem('lastRiderPos');
            if (cached) {
                rider = JSON.parse(cached); // { lat, lng }
            }
        } finally {
            // 최종적으로 다시 구매자한테 라이더의 좌표 전송
            webSocket.send(
                JSON.stringify({
                    handlerType: 'riderLocResponseHandler',
                    riderMemberNo,
                    buyerMemberNo,
                    lat: rider.lat,
                    lng: rider.lng,
                })
            );
            console.log('riderLocResponseHandler 전송');
        }
    },

    riderLocResponseHandler: (data) => {
        console.log(data);
    },
};

// 웹소켓 받는 부분
webSocket.addEventListener('message', (msg) => {
    const data = JSON.parse(msg.data);
    console.log(data);
    const handler = riderHandlers[data.handlerType];
    if (handler) {
        handler(data);
    } else {
        console.warn('알 수 없는 handlerType:', data.handlerType);
    }
});
