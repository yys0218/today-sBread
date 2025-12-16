// 공통 alert 함수 (에러 메시지 + focus)
function alertFocus(msg, element) {
    Swal.fire({
        icon: 'error',
        title: '입력 오류',
        text: msg,
        confirmButtonText: '확인',
        didClose: () => {
            element.focus();
        },
    });
}

// 비밀번호 표시 토글
$('#pwToggleIcon').on('click', function () {
    let pwInput = $('#memberPw');
    if (pwInput.attr('type') === 'password') {
        pwInput.attr('type', 'text');
        $(this).removeClass('bi-eye-fill').addClass('bi-eye-slash-fill');
    } else {
        pwInput.attr('type', 'password');
        $(this).removeClass('bi-eye-slash-fill').addClass('bi-eye-fill');
    }
});

$('#pwReToggleIcon').on('click', function () {
    let pwInput = $('#memberPwRe');
    if (pwInput.attr('type') === 'password') {
        pwInput.attr('type', 'text');
        $(this).removeClass('bi-eye-fill').addClass('bi-eye-slash-fill');
    } else {
        pwInput.attr('type', 'password');
        $(this).removeClass('bi-eye-slash-fill').addClass('bi-eye-fill');
    }
});

// 유효성 플래그
let idValid = false;
let pwValid = false;
let pwReValid = false;
let emailValid = false;
let phoneValid = false;

// 검증 순서 정의
let validationOrder = [
    { name: 'memberId', msg: '아이디는' },
    { name: 'memberPw', msg: '비밀번호는' },
    { name: 'memberPwRe', msg: '비밀번호 확인은' },
    { name: 'memberName', msg: '이름은' },
    { name: 'memberBirth', msg: '생년월일은' },
    { name: 'emailLocal', msg: '이메일은' },
    { name: 'emailDomain', msg: '도메인은' },
    { name: 'memberAddress', msg: '주소는' },
    { name: 'memberPhone', msg: '전화번호는' },
];

// 폼 검증 함수
function validateForm() {
    for (let item of validationOrder) {
        let element = $(`[name="${item.name}"]`);
        let value = element.val()?.trim();
        if (!value) {
            alertFocus(item.msg + ' 필수 입력 항목입니다.', element);
            return false; // 첫 에러에서 멈춤
        }
    }

    // 중복검사 플래그 확인
    if (!idValid) {
        ValidAlert(1);
        return false;
    }
    if (!pwValid) {
        ValidAlert(2);
        return false;
    }
    if (!pwReValid) {
        ValidAlert(3);
        return false;
    }
    if (!emailValid) {
        ValidAlert(4);
        return false;
    }
    if (!phoneValid) {
        ValidAlert(5);
        return false;
    }

    return true;
}

// 최종 submit 버튼 클릭 시
$('#ckInput').on('click', function (e) {
    e.preventDefault();

    if (validateForm()) {
        const phone = $('#memberPhone');
        phone.val(phone.val().replace(/\D/g, '')); // 숫자만 남김
        $('#form').submit();
    }
});

// ValidAlert 함수
function ValidAlert(typeNo) {
    let validErrorMsg = {
        1: '아이디는',
        2: '비밀번호는',
        3: '비밀번호 확인은',
        4: '이메일은',
        5: '전화번호는',
    };
    Swal.fire({
        icon: 'error',
        title: '입력 오류',
        text: validErrorMsg[typeNo] + ' 필수 입력 항목입니다.',
        confirmButtonText: '확인',
    });
}

// 아이디 중복 체크 해야하는 곳
$('#memberId').on('change', function () {
    idValid = false;
    let memberIdElement = $('#memberId');
    let checkI = $(this).siblings('i');
    let memberId = memberIdElement.val().trim();
    if (!memberId) {
        checkI.removeClass('feedback-t');
        checkI.addClass('d-none');
        return;
    }
    $.ajax({
        url: '/rider/ajaxDuplicationId',
        type: 'post',
        data: { memberId: memberId },
        success: function (response) {
            console.log(response);
            if (response === true || response === 'true') {
                checkI.text('사용 가능한 아이디 입니다.');
                checkI.addClass('feedback-t');
                checkI.removeClass('d-none');
                idValid = true;
            } else {
                checkI.text('중복된 아이디 입니다.');
                checkI.removeClass('feedback-t');
                checkI.removeClass('d-none');
            }
        },
    });
});

// 비밀번호 유효성 검사
$('#memberPw').on('input', function () {
    pwValid = false;
    //(?=.*[A-Z])
    let regex = /^(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+~`\-={}[\]:;"'<>,.?/\\]).{8,15}$/;
    let pwElement = $(this);
    let pw = pwElement.val().trim();
    let pwI = $(this).siblings('i');
    if (regex.test(pw)) {
        pwI.first().text('사용 가능한 비밀번호 입니다.');
        pwI.first().addClass('feedback-t');
        pwI.first().removeClass('d-none');
        pwValid = true;
    } else {
        pwI.first().first().text('8~12글자 하나의 숫자 , 특수문자');
        pwI.first().removeClass('feedback-t');
        pwI.first().removeClass('d-none');
    }
    ChkPwRe();
});

// 비밀번호 확인 중복검사
$('#memberPwRe').on('change', function () {
    ChkPwRe();
});
function ChkPwRe() {
    pwReValid = false;
    let pwElement = $('#memberPw');
    let pwReElement = $('#memberPwRe');
    let pw = pwElement.val().trim();
    let pwRe = pwReElement.val().trim();
    let pwReI = $('#memberPwRe').siblings('i');
    if (pw && pwRe) {
        if (pw === pwRe) {
            pwReI.first().text('비밀번호가 일치합니다.');
            pwReI.first().addClass('feedback-t');
            pwReI.first().removeClass('d-none');
            pwReValid = true;
        } else {
            pwReI.first().text('비밀번호를 확인해주세요.');
            pwReI.first().removeClass('feedback-t');
            pwReI.first().removeClass('d-none');
            pwReValid = false;
        }
    } else {
        if (!pw && pwRe) {
            pwReI.first().text('비밀번호를 확인해주세요.');
            pwReI.first().removeClass('feedback-t');
            pwReI.first().removeClass('d-none');
            pwReValid = false;
        }
    }
}

// 전화번호 - 달아주기
function formatKrPhone(digits) {
    if (!digits) return '';
    // 일반(010/0XX) 규칙
    if (digits.length <= 3) return digits;
    if (digits.length <= 7) return digits.replace(/^(\d{3})(\d{0,4})$/, '$1-$2'); // 010-XXXX
    if (digits.length <= 11) return digits.replace(/^(\d{3})(\d{3,4})(\d{0,4})$/, '$1-$2-$3'); // 010-XXX(X)-XXXX
    return digits.slice(0, 11).replace(/^(\d{3})(\d{4})(\d{4})$/, '$1-$2-$3'); // 최대 11자리
}

// 타이핑할 때 화면에만 하이픈 표시
$('#memberPhone').on('input', function () {
    const raw = this.value.replace(/\D/g, '').slice(0, 11); // 숫자만, 최대 11자리
    this.value = formatKrPhone(raw);
});

// 전화번호 중복검사
$('#memberPhone').on('change', function () {
    phoneValid = false;
    let phone = $(this).val().replace(/\D/g, '');
    phoneI = $(this).siblings('i');

    if (phone.length === 11) {
        $.ajax({
            url: '/rider/ajaxDuplicationPhone',
            type: 'POST',
            data: { memberPhone: phone },
            success: function (response) {
                if (response === true || response === 'true') {
                    phoneI.text('사용가능한 전화번호입니다.');
                    phoneI.addClass('feedback-t');
                    phoneI.removeClass('d-none');
                    phoneValid = true;
                } else {
                    phoneI.text('중복된 전화번호입니다.');
                    phoneI.removeClass('feedback-t');
                    phoneI.removeClass('d-none');
                }
            },
            error: function (xhr, status, message) {},
        });
    } else {
        phoneI.text('전화번호 형식이 일치하지않습니다.');
        phoneI.removeClass('feedback-t');
        phoneI.removeClass('d-none');
    }
});

// 생년월일이 미래날씨일 경우
$('#memberBirth').on('change', function () {
    let now = new Date();
    now.setHours(0, 0, 0, 0);
    let birth = $('#memberBirth').val().trim();
    console.log(birth);
    let [y, m, d] = birth.split('-').map(Number);
    let birthDate = new Date(y, m - 1, d);
    console.log([y, m, d]);
    console.log('now : ' + now.getDate());
    console.log('birth : ' + birthDate.getDate());

    if (birthDate > now) {
        console.log('일어날수 없는 일');
        alertFocus('생일이 오늘보다 뒤일 수 없습니다.', $(this));
    } else if (birthDate.getTime() == now.getTime()) {
        console.log('같은날');
        alertFocus('생일이 오늘과 같은 날일 수 없습니다.', $(this));
    } else {
        console.log('오늘이 더 작은날');
    }
});

// // form의 값이 비어있을 경우 띄울 alert 함수
function formCheckAlert(element) {
    let formErrorMsg = {
        memberId: '아이디는',
        memberPw: '비밀번호는',
        memberPwRe: '비밀번호 확인은',
        memberName: '이름은',
        emailLocal: '이메일은',
        emailDomain: '도메인은',
        memberAddress: '주소는',
        memberPhone: '전화번호는',
    };
    // 요소의 name값을 변수에 지정
    let eName = element.attr('name');
    // alert창에서 msg내용변환을 위해 값 저장용
    let msg = formErrorMsg[eName] || '해당 항목은';
    // 해당하는 name값에 맞는 text내용 지정

    // alert 내용 작성
    Swal.fire({
        icon: 'error', // 오류 아이콘
        title: '입력 오류', // 알림 제목
        text: msg + ' 필수 입력 항목입니다.', // 알림 메시지
        confirmButtonText: '확인', // 버튼 내용
        didClose: () => {
            // 모달이 닫히고 나서 실행할 행동
            element.focus(); // 모달이 완전히 닫힌 후 포커스
        },
    });
}

// 이메일 도메인 SELECT 값 변경 시
$('#emailDomainSelect').on('change', function () {
    let domain = $(this).val().trim();
    const domainInput = $('#emailDomain');
    domainInput.val(domain);
});
const modal = $('#emailModal');
const openBtn = $('#openModalBtn');
const closeBtn = $('.closeBtn');
const submitBtn = $('#submitAuthCode');
let mailCode;

// 이메일 중복확인 클릭시
$('#ckEmail').on('click', function () {
    emailValid = false;
    let localElement = $('#emailLocal'); // 이메일 input 요소
    let local = localElement.val(); // 이메일 요소의 value
    let domainElement = $('#emailDomain'); // 도메인 input 요소
    let domain = domainElement.val().trim(); // 도메인 요소의 value
    let memberEmail = local + '@' + domain; // 합친 이메일 주소
    let regex = /^.*@.*/;

    // 이메일 , 도메인칸이 하나라도 비워져있을 경우
    if (!local || !domain) {
        if (!local) {
            // Alert 사용 후 비어있는 칸에 포커스
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '이메일 입력칸이 비워져있습니다.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    localElement.focus();
                },
            });
        } else {
            // Alert 사용 후 비어있는 칸에 포커스
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '도메인 입력칸이 비워져있습니다.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    domainElement.focus();
                },
            });
        }
        return;
    } // if문 종료

    if (local.includes('@')) {
        console.log('@통과');
        // Alert 사용 후 해당 칸에 포커스
        Swal.fire({
            icon: 'error', // 오류 아이콘
            title: '입력 오류', // 알림 제목
            text: '@를 사용할 수 없습니다.', // 알림 메시지
            confirmButtonText: '확인', // 버튼 내용
            didClose: () => {
                localElement.focus();
            },
        });
        return;
    } // if문 종료

    if (!domain.includes('.')) {
        // Alert 사용 후 해당 칸에 포커스
        Swal.fire({
            icon: 'error', // 오류 아이콘
            title: '입력 오류', // 알림 제목
            html: '도메인 형식이 올바르지 않습니다.<br>예) naver.com ', // 알림 메시지
            confirmButtonText: '확인', // 버튼 내용
            didClose: () => {
                domainElement.focus();
            },
        });
        return;
    } // if문 종료

    if (local && domain) {
        Swal.fire({
            title: '이메일 전송 중...',
            text: '잠시만 기다려 주세요.',
            allowOutsideClick: false, // 바깥 클릭 금지
            didOpen: () => {
                Swal.showLoading(); // 로딩 스피너 표시
            },
        });
        $.ajax({
            url: '/rider/ajaxDuplicationEmail',
            type: 'POST',
            data: { memberEmail },
            success: function (response) {
                Swal.close(); // 로딩 닫기
                console.log(response);
                let code;
                if (response == 'NotDuplicationEmail') {
                    showErrorAlert('중복된 이메일 입니다.');
                } else if (response == 'MailSendFail') {
                    showErrorAlert('이메일 전송에 실패했습니다. 잠시후 시도해주세요');
                } else {
                    code = response;
                    mailCode = response;
                    $('#authMsg').text('');
                    modal.css('display', 'flex'); // jQuery로 CSS 변경
                    authTime();
                }
            },
            error: function (xhr, status, message) {},
        });
        // showSuccessAlert(memberEmail);
        return;
    } // if문 종료
    // ajax로 이메일로 인증번호 전송하기.
});

// 모달 닫기 (X 클릭)
closeBtn.on('click', function () {
    modal.css('display', 'none');
});

// 바깥 영역 클릭 시 닫기
$(window).on('click', function (e) {
    // e.target이 모달 배경(#emailModal)과 같은 경우만 닫기
    if (e.target.id === 'emailModal') {
        // modal.css('display', 'none');
    }
});

// 인증번호 제출
submitBtn.on('click', function () {
    if (mailCode == null) {
        $('#authMsg').text('인증 시간 만료');
        $('#authMsg').css('color', 'red');
    } else {
        const authCode = $('#authCodeInput').val();
        if (authCode == mailCode) {
            $('#authMsg').prop('readonly', true);
            $('#authMsg').text('인증완료');
            $('#authMsg').css('color', 'green');
            window.clearInterval(intervalId);
            $('#memberEmail').prop('readonly', true);
            $('#openModalBtn').prop('disabled', true);
            emailValid = true; // ajax로 이동하기
            // modal.css('display', 'none');
        } else {
            $('#authMsg').text('인증실패');
            $('#authMsg').css('color', 'red');
        }
    }
});
let intervalId;
function authTime() {
    $('.modal-time').html("<span>남은시간 </span><span id='min'>01</span> : <span id='sec'>00</span>");
    intervalId = window.setInterval(function () {
        timeCount();
    }, 1000);
}

function timeCount() {
    const min = $('#min').text();
    const sec = $('#sec').text();
    if (sec == '00') {
        if (min != '0') {
            const newMin = Number(min) - 1;
            $('#min').text(newMin);
            $('#sec').text(59);
        } else {
            window.clearInterval(intervalId);
            mailCode = null;
            $('#authMsg').text('인증 시간 만료');
            $('#authMsg').css('color', 'red');
            $('.modal-time').empty();
        }
    } else {
        const newSec = Number(sec) - 1;
        if (newSec < 10) {
            $('#sec').text('0' + newSec);
        } else {
            $('#sec').text(newSec);
        }
    }
}

$('#roadAddress').on('click', function () {
    sample4_execDaumPostcode();
});
// 화면 중앙 정렬을 위한 팝업 설정
var width = 500; //팝업의 너비
var height = 600; //팝업의 높이
//본 예제에서는 도로명 주소 표기 방식에 대한 법령에 따라, 내려오는 데이터를 조합하여 올바른 주소를 구성하는 방법을 설명합니다.
function sample4_execDaumPostcode() {
    new daum.Postcode({
        // 검색된 주소와 내려가는 데이터의 '시','도' 부분을 축약 표시합니다(한글 주소만 해당). 기본값은 true로 설정되어 있습니다.
        // (서울특별시 -> 서울, 광주광역시 -> 광주, 단, '세종특별자치시','제주특별자치도','강원특별자치도','전북특별자치도'는 지자체의 요청에 의해 제외)
        shorthand: false,
        onclose: function (state) {
            //state는 우편번호 찾기 화면이 어떻게 닫혔는지에 대한 상태 변수 이며, 상세 설명은 아래 목록에서 확인하실 수 있습니다.
            if (state === 'FORCE_CLOSE') {
                //사용자가 브라우저 닫기 버튼을 통해 팝업창을 닫았을 경우, 실행될 코드를 작성하는 부분입니다.
            } else if (state === 'COMPLETE_CLOSE') {
                //사용자가 검색결과를 선택하여 팝업창이 닫혔을 경우, 실행될 코드를 작성하는 부분입니다.
                //oncomplete 콜백 함수가 실행 완료된 후에 실행됩니다.
            }
        },
        oncomplete: function (data) {
            // 팝업에서 검색결과 항목을 클릭했을때 실행할 코드를 작성하는 부분.

            // 도로명 주소의 노출 규칙에 따라 주소를 표시한다.
            // 내려오는 변수가 값이 없는 경우엔 공백('')값을 가지므로, 이를 참고하여 분기 한다.
            var roadAddr = data.roadAddress; // 도로명 주소 변수

            // 우편번호와 주소 정보를 해당 필드에 넣는다.

            document.getElementById('roadAddress').value = roadAddr;
        },
    }).open({
        left: window.screen.width / 2 - width / 2, // 화면 좌측 중앙 정렬
        top: window.screen.height / 2 - height / 2, // 화면 상단 중앙 정렬
        popupKey: 'daumApi0915', // key값을 활용하여 중복 팝업창 방지(아무 값이나 넣어도 됌)
    });
}

$('#backBtn').on('click', function () {
    window.location.href = '/rider';
});
