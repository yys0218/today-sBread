/*
	┌────────────────────┐
	│  만든이 : 안병주   │
	└────────────────────┘
    [CDN]
    <script src="https://code.jquery.com/jquery-latest.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>

	아이콘 값(icon)	│	의미/용도				│	예시
	────────────────┼───────────────────────┼───────────────────────────
	success			│	성공 알림				│	✅ 작업 성공, 저장 완료 등
	error			│	오류 알림				│	❌ 실패, 에러 발생 등
	warning			│	경고 메세지				│	⚠️ 주의, 위험성 안내 등
	info			│	정보 메세지				│	ℹ️ 일반 정보, 설명 등
	question		│	사용자 선택 필요			│	❓ 확인창, 질문 등
	────────────────┴───────────────────────┴───────────────────────────
    모달 강제로 닫기

    if (Swal.isVisible()) { // SweetAlert 열려있는지 체크
        Swal.close(); // 열려 있으면 닫기
    }
    
	사용법 :  메세지 , url
	showSuccessAlert(msg,url);
	showErrorAlert(msg,url);
	showWarningAlert(msg,url);
	showInfoAlert(msg,url);
	showToast(msg,icon,time(ms));
	showConfirmAlert(
	        '삭제하시겠습니까?', // 표시할 메시지
	        () => {
	          // 사용자가 '예'를 눌렀을 때 실행
	          console.log('확인 클릭됨');
	          showToast('삭제 완료!', 'success'); // 토스트도 같이 표시 (showSuccessAlert도 사용가능)
	        },
	        () => {
	          // 사용자가 '아니오'를 눌렀을 때 실행
	          console.log('취소 클릭됨');
	          showToast('삭제 취소됨', 'error');
	        }
	      );
		  
	사용법 : Title 메세지 , url
	showSuccessTitleAlert(title,msg,url);
	showErrorTitleAlert(title,msg,url);
	showWarningTitleAlert(title,msg,url);
	showInfoTitleAlert(title,msg,url);
	showTitleToast(title,msg,icon,time(ms));
	showConfirmTitleAlert(
			title = '제목입니다.',
			msg = '정말로 진행하시겠습니까?',
	        () => {
	          // 사용자가 '예'를 눌렀을 때 실행
	          console.log('확인 클릭됨');
	          showToast('삭제 완료!', 'success'); // 토스트도 같이 표시 (showSuccessAlert도 사용가능)
	        },
	        () => {
		      // 사용자가 '아니오'를 눌렀을 때 실행
	          console.log('취소 클릭됨');
	          showToast('삭제 취소됨', 'error');
	       }
		  	      );
	
*/

// ✅ [0] jQuery가 없으면 자동으로 CDN 삽입
(function loadJQueryCDN() {
    if (typeof window.jQuery === 'undefined') {
        const script = document.createElement('script');
        script.src = 'https://code.jquery.com/jquery-latest.min.js'; // 필요에 따라 버전 변경 가능
        script.defer = false; // HTML 파싱 후 실행하는게 아닌 head에 추가 되자마자 로딩하도록 설정

        document.head.appendChild(script);
    }
})();

// ✅ [1] SweetAlert2 라이브러리를 동적으로 로드하는 함수
(function loadSweetAlertCDN() {
    if (typeof Swal === 'undefined') {
        const script = document.createElement('script'); // <script> 태그 생성
        script.src = 'https://cdn.jsdelivr.net/npm/sweetalert2@11'; // 라이브러리 주소 설정
        script.defer = false; // HTML 파싱 후 실행하는게 아닌 head에 추가 되자마자 로딩하도록 설정
        document.head.appendChild(script); // <head>에 추가하여 로드
    }
})();

// ✅ [2] SweetAlert2가 로드될 때까지 기다리는 함수
function waitForSwal(callback) {
    const check = () => {
        if (typeof Swal !== 'undefined') {
            callback(); // Swal 로드 완료 시 콜백 실행
        } else {
            setTimeout(check, 50); // 아직 로드 안 됐으면 50ms 후 재시도
        }
    };
    check(); // 체크 시작
}

// 포커스용 알림창
function focusAlert(msg = '성공하였습니다', icon = 'success') {
    return Swal.fire({
        icon: icon, // 아이콘
        title: msg, // 알림 메시지
        confirmButtonText: '확인', // 확인 버튼 텍스트
        confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
    });
}

// 성공 메시지 알림창 표시 함수
function showSuccessAlert(msg = '성공하였습니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'success', // 성공 아이콘
            title: msg, // 알림 메시지
            confirmButtonText: '확인', // 확인 버튼 텍스트
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl; // URL이 있으면 이동
        });
    });
}

// 오류 메시지 알림창 표시 함수
function showErrorAlert(msg = '오류가 발생했습니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'error', // 오류 아이콘
            title: msg,
            confirmButtonText: '닫기',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl;
        });
    });
}

// 경고 메시지 알림창 표시 함수
function showWarningAlert(msg = '주의가 필요합니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'warning', // 경고 아이콘
            title: msg,
            confirmButtonText: '확인',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl;
        });
    });
}

// 경고 메시지 알림창 표시 함수
function showCostomWarningAlert(msg = '주의가 필요합니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'warning', // 경고 아이콘
            title: msg,
            confirmButtonText: '확인',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
            customClass: {
                container: 'my-swal-container',
            },
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl;
        });
    });
}

// 정보 안내 메시지 알림창 표시 함수
function showInfoAlert(msg = '안내 메시지입니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'info', // 정보 아이콘
            title: msg,
            confirmButtonText: '확인',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl;
        });
    });
}

// 확인/취소 다이얼로그 (사용자의 선택에 따라 함수 실행 가능)
function showConfirmAlert(
    msg = '정말로 진행하시겠습니까?',
    onConfirm = () => {}, // 확인 시 실행할 함수
    onCancel = () => {} // 취소 시 실행할 함수
) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'question', // 물음표 아이콘
            title: msg,
            showCancelButton: true, // 취소 버튼 표시
            confirmButtonText: '예',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
            cancelButtonText: '아니오',
        }).then((result) => {
            if (result.isConfirmed) {
                onConfirm(); // 확인 선택 시 콜백 실행
            } else {
                onCancel(); // 취소 선택 시 콜백 실행
            }
        });
    });
}

// 우측 상단에 간단히 나타나는 토스트 메시지
function showToast(msg = '처리되었습니다.', type = 'success', duration = 2000) {
    waitForSwal(() => {
        const Toast = Swal.mixin({
            toast: true, // 토스트 스타일로 표시
            position: 'top-end', // 위치: 화면 오른쪽 상단
            showConfirmButton: false, // 확인 버튼 숨김
            timer: duration, // 표시 시간(ms)
            timerProgressBar: true, // 진행 바 표시
        });
        Toast.fire({
            icon: type, // 아이콘 종류 ('success', 'error', etc.)
            title: msg,
        });
    });
}

// 로딩 모달 열기 함수
function showLoadingModal(title = '처리 중...', message = '잠시만 기다려주세요.', duration = 5000) {
    waitForSwal(() => {
        Swal.fire({
            title: title,
            text: message,
            allowOutsideClick: false,
            allowEscapeKey: false,
            backdrop: true,
            timer: duration, // 자동 닫기 시간 (ms);
            didOpen: () => {
                Swal.showLoading();
            },
        });
    });
}
// 모달 강제로 닫기

// if (Swal.isVisible()) {
//     Swal.close(); // 열려 있으면 닫기
// }

// 성공 제목 , 메시지 알림창 표시 함수
function showSuccessTitleAlert(title = '제목입니다.', msg = '성공하였습니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'success', // 성공 아이콘
            title: title, // 알림 제목
            text: msg, // 알림 메시지
            confirmButtonText: '확인', // 확인 버튼 텍스트
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl; // URL이 있으면 이동
        });
    });
}

// 오류 제목 , 메시지 알림창 표시 함수
function showErrorTitleAlert(title = '제목입니다.', msg = '오류가 발생했습니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'error', // 오류 아이콘
            title: title, // 알림 제목
            text: msg, // 알림 메시지
            confirmButtonText: '닫기',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl;
        });
    });
}

//  경고 제목 , 메시지 알림창 표시 함수
function showWarningTitleAlert(title = '제목입니다.', msg = '주의가 필요합니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'warning', // 경고 아이콘
            title: title, // 알림 제목
            text: msg, // 알림 메시지
            confirmButtonText: '확인',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl;
        });
    });
}

// 정보 제목 , 안내 메시지 알림창 표시 함수
function showInfoTitleAlert(title = '제목입니다.', msg = '안내 메시지입니다.', redirectUrl = null) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'info', // 정보 아이콘
            title: title, // 알림 제목
            text: msg, // 알림 메시지
            confirmButtonText: '확인',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
        }).then(() => {
            if (redirectUrl) location.href = redirectUrl;
        });
    });
}

// 제목 , 확인/취소 다이얼로그 (사용자의 선택에 따라 함수 실행 가능)
function showConfirmTitleAlert(
    title = '제목입니다.',
    msg = '정말로 진행하시겠습니까?',
    onConfirm = () => {}, // 확인 시 실행할 함수
    onCancel = () => {} // 취소 시 실행할 함수
) {
    waitForSwal(() => {
        Swal.fire({
            icon: 'question', // 물음표 아이콘
            title: title, // 알림 제목
            html: msg, // 알림 메시지
            showCancelButton: true, // 취소 버튼 표시
            confirmButtonText: '예',
            confirmButtonColor: '#ff6600', // 강조 버튼 (주황)
            cancelButtonText: '아니오',
        }).then((result) => {
            if (result.isConfirmed) {
                onConfirm(); // 확인 선택 시 콜백 실행
            } else {
                onCancel(); // 취소 선택 시 콜백 실행
            }
        });
    });
}

// 제목 , 우측 상단에 간단히 나타나는 토스트 메시지
function showTitleToast(title = '제목입니다.', msg = '처리되었습니다.', type = 'success', duration = 2000) {
    waitForSwal(() => {
        const Toast = Swal.mixin({
            toast: true, // 토스트 스타일로 표시
            position: 'top-end', // 위치: 화면 오른쪽 상단
            showConfirmButton: false, // 확인 버튼 숨김
            timer: duration, // 표시 시간(ms)
            timerProgressBar: true, // 진행 바 표시
        });
        Toast.fire({
            icon: type, // 아이콘 종류 ('success', 'error', etc.)
            title: title, // 알림 제목
            text: msg, // 알림 메시지
        });
    });
}
