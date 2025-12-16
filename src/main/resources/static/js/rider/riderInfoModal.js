const deleteModal = $('#riderDeleteModal');
const deliveryFeeModal = $('#riderDeliveryFeeModal');

// 회원 탈퇴 버튼
$(document).on('click', '#deleteModalShow', function () {
    emptyDeleteModal();
    deleteModal.show();
});

$(document).on('input', 'input:radio[name=reason]', function () {
    // reasonOther이 체크되어있을때 실행
    if ($("input:radio[id='reasonOther']").prop('checked')) {
        $('#otherText').show();
    } else {
        $('#otherText').hide();
    }
});

// 모달 닫기버튼 누를시 모달 닫기
$(document).on('click', '#deleteModalHide', function () {
    deleteModal.hide();
});

// 모달의 내용을 비우고 처음 연것처럼 만들어줄 함수
function emptyDeleteModal() {
    // input type=radio의 name이 reason인 모든 체크박스를 fasle로 변경
    $('input:radio[name=reason]').prop('checked', false);
    // input type=checkbox의 name이 agree인 체크박스를 fasle로 변경
    $('input:checkbox[name=agree]').prop('checked', false);
    // textarea의 값을 지우고 해당 요소를 숨김
    $('#otherText').val('').hide();
}

const infoModal = $('#changeRiderInfoModal');
const originalAddress = $('#modalAddress1').val().trim();
const originalName = $('#modalName').val().trim();
const originalEmail = $('#modalEmail').val().trim();
const originalPhone = $('#modalPhone').val().trim();

let idValid = true;
let phoneValid = true;
let emailValid = true;
let addressValid = true;
let password1Valid = true;
// let password2Valid = true;

$('#updateMemberInfoBtn').on('click', function () {
    const changeAddress = $('#modalAddress2').length === 0 ? $('#modalAddress1').val().trim() : $('#modalAddress1').val().trim() + $('#modalAddress2').val().trim();
    const changeName = $('#modalName').val().trim();
    const changeEmail = $('#modalEmail').val().trim();
    const changePhone = $('#modalPhone').val().trim();
    const changePassword = $('#modalPassword1').length !== 0 && $('#modalPassword2').length !== 0 ? ($('#modalPassword1').val().trim() === $('#modalPassword2').val().trim() ? $('#modalPassword2').val().trim() : '') : '';
    let updateData = {};
    if (originalAddress !== changeAddress) updateData.address = changeAddress;
    if (originalName !== changeName) updateData.name = changeName;
    if (originalEmail !== changeEmail) updateData.email = changeName;
    if (originalPhone !== changePhone) updateData.phone = changePhone;
    if (changePassword !== '') updateData.password = changePassword;

    const checks = [idValid, phoneValid, emailValid, addressValid, password1Valid];

    const hasError = checks.some((element, index) => {
        console.log('실행');
        console.log('index : ' + index);
        console.log('element : ' + element);
        if (!element) {
            switch (index) {
                case 0:
                    console.log('case0');
                    Swal.fire({
                        icon: 'error', // 오류 아이콘
                        title: '입력 오류', // 알림 제목
                        text: '아이디를 확인해주세요', // 알림 메시지
                        confirmButtonText: '확인', // 버튼 내용
                        didClose: () => {
                            $('#modalId').focus();
                        },
                    });
                    break;
                case 1:
                    console.log('case1');
                    Swal.fire({
                        icon: 'error', // 오류 아이콘
                        title: '입력 오류', // 알림 제목
                        text: '전화번호 확인해주세요', // 알림 메시지
                        confirmButtonText: '확인', // 버튼 내용
                        didClose: () => {
                            $('#modalPhone').focus();
                        },
                    });
                    break;
                case 2:
                    console.log('case2');
                    Swal.fire({
                        icon: 'error', // 오류 아이콘
                        title: '입력 오류', // 알림 제목
                        text: '이메일를 확인해주세요', // 알림 메시지
                        confirmButtonText: '확인', // 버튼 내용
                        didClose: () => {
                            $('#modalEmail').focus();
                        },
                    });
                    break;
                case 3:
                    console.log('case3');
                    Swal.fire({
                        icon: 'error', // 오류 아이콘
                        title: '입력 오류', // 알림 제목
                        text: '주소를 확인해주세요', // 알림 메시지
                        confirmButtonText: '확인', // 버튼 내용
                        didClose: () => {
                            $('#modalAddress').focus();
                        },
                    });
                    break;
                case 4:
                    console.log('case4');
                    Swal.fire({
                        icon: 'error', // 오류 아이콘
                        title: '입력 오류', // 알림 제목
                        text: '비밀번호를 확인해주세요', // 알림 메시지
                        confirmButtonText: '확인', // 버튼 내용
                        didClose: () => {
                            $('#modalPassword2').focus();
                        },
                    });
                    break;
            }
            return true;
        }
        return false;
    });
    if (hasError) return;

    if (Object.keys(updateData).length === 0) {
        showErrorTitleAlert('오류', '수정된 값이 없습니다.');
    } else {
        // $.ajax({
        //     url: '/rider/ajaxUpdateMemberInfo',
        //     type: 'post',
        //     contentType: 'application/json',
        //     data: JSON.stringify(updateData),
        //     success: function (response) {
        //         if (response == '회원 정보 수정 완료') {
        //             location.reload();
        //         }
        //     },
        //     error: function (xhr, status, message) {},
        // });
        // console.log(updateData);
    }
});

$('#infoModalShow').on('click', function () {
    infoModal.show();
});

$('.close_modal,.close-btn').on('click', function () {
    $('#modalName').val(originalName);
    $('#modalPhone').val(originalPhone);
    $('#modalEmail').val(originalEmail);
    $('#modalAddress1').val(originalAddress);
    if ($('#modalAddress2').length) {
        $('#modalAddress1').closest('.delivery_title').find('span').removeAttr('style');
        $('#modalAddress2').remove();
    }
    $('#ckEmail').length ? $('#ckEmail').remove() : '';
    if ($('#modalPassword1').length) {
        $('#modalPassword1').closest('.delivery_title').remove();
        $('#modalPassword2').closest('.delivery_title').remove();
        $('#cancelPw').remove();
        $('.delivery_title').last().after(`<div class="btn" id="changePw" style="color: #fff; border: 1px solid">비밀번호 변경</div>`);
    }
    infoModal.hide();
    $('#riderDeliveryFeeModal').hide();
});

$(document).on('click', '#changePw', function () {
    password1Valid = false;
    password1Valid = false;
    const pwInputs = `<div class="delivery_title">
                    <span>비밀번호</span>
                    <div class="d-flex">
                        <input type="password" name="text" id="modalPassword1" class="custom-r-input" />
                    </div>
                </div>
                <div class="delivery_title">
                    <span>비밀번호 확인</span>
                    <div class="d-flex">
                        <input type="password" name="text" id="modalPassword2" class="custom-r-input" />
                    </div>
                </div>
                <div class="btn" id="cancelPw" style="color: #fff; border: 1px solid">취소</div>
                `;
    $(this).remove();
    $('.delivery_title').last().after(pwInputs);
});

$(document).on('click', '#cancelPw', function () {
    password1Valid = true;
    password1Valid = true;
    $('#modalPassword1').closest('.delivery_title').remove();
    $('#modalPassword2').closest('.delivery_title').remove();
    $(this).remove();
    const changePw = `<div class="btn" id="changePw" style="color: #fff; border: 1px solid">비밀번호 변경</div>`;
    $('.delivery_title').last().after(changePw);
});

function addressInputs(changeAddress) {
    console.log('함수실행');
    console.log(originalAddress);
    console.log(changeAddress);
    if (originalAddress !== changeAddress) {
        if ($('#modalAddress2').length === 0) {
            console.log('여기 실행');
            const addressInput = `<input type="text" name="text" id="modalAddress2" class="custom-r-input" />`;
            $('#modalAddress1').closest('.delivery_title').find('span').attr('style', 'display: inline-block; vertical-align: top; padding-top: 15px; height: 100px');
            $('#modalAddress1').after(addressInput);
        } else {
            $('#modalAddress2').val('');
        }
    } else {
        const addAddressInput = $('#modalAddress2');
        if (addAddressInput) {
            console.log('저기 실행');
            $('#modalAddress1').closest('.delivery_title').find('span').removeAttr('style');
            addAddressInput.remove();
        }
    }
}

const OriginalEmail = $('#modalEmail').val().trim();

// 비밀번호 유효성 검사
$(document).on('input', '#modalPassword1', function () {
    password1Valid = false;
    //(?=.*[A-Z])
    let regex = /^(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+~`\-={}[\]:;"'<>,.?/\\]).{8,15}$/;
    let pwElement = $(this);
    console.log(pwElement);
    let pw = pwElement.val().trim();
    // let pwI = $(this).siblings('i');
    if (regex.test(pw)) {
        console.log('실행');
        // pwI.first().text('사용 가능한 비밀번호 입니다.');
        // pwI.first().addClass('feedback-t');
        // pwI.first().removeClass('d-none');
        password1Valid = true;
    } else {
        // pwI.first().first().text('8~12글자 하나의 숫자 , 특수문자');
        // pwI.first().removeClass('feedback-t');
        // pwI.first().removeClass('d-none');
    }
    ChkPwRe();
});

// 비밀번호 확인 중복검사
$('#modalPassword2').on('change', function () {
    ChkPwRe();
});

function ChkPwRe() {
    password1Valid = false;
    let pwElement = $('#modalPassword1');
    let pwReElement = $('#modalPassword2');
    let pw = pwElement.val().trim();
    let pwRe = pwReElement.val().trim();
    //let pwReI = $('#memberPwRe').siblings('i');
    if (pw && pwRe) {
        if (pw === pwRe) {
            // pwReI.first().text('비밀번호가 일치합니다.');
            // pwReI.first().addClass('feedback-t');
            // pwReI.first().removeClass('d-none');
            password1Valid = true;
        } else {
            // pwReI.first().text('비밀번호를 확인해주세요.');
            // pwReI.first().removeClass('feedback-t');
            // pwReI.first().removeClass('d-none');
            password1Valid = false;
        }
    } else {
        if (!pw && pwRe) {
            // pwReI.first().text('비밀번호를 확인해주세요.');
            // pwReI.first().removeClass('feedback-t');
            // pwReI.first().removeClass('d-none');
            password1Valid = false;
        }
    }
}

// 이메일 모달 닫기 (X 클릭)
$('.closeBtn').on('click', function () {
    $('#emailModal').css('display', 'none');
    $('#riderDeliveryFeeModal').css('display', 'none');
});

$('#modalEmail').on('change', function () {
    let changeEmail = $('#modalEmail').val().trim(); // 이메일 input 요소
    if (OriginalEmail !== changeEmail) {
        emailValid = false;
        const emailBtn = `<div class="btn" id="ckEmail" style="color: #fff">이메일 인증</div>`;
        $(this).closest('.delivery_title').after(emailBtn);
    } else {
        emailValid = true;
        const ckBtn = $('#ckEmail');
        ckBtn.remove();
    }
});

// 인증번호 제출
$('#submitAuthCode').on('click', function () {
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
            $('#modalEmail').prop('readonly', true);
            $('#ckEmail').remove();
            emailValid = true; // ajax로 이동하기
            $('#emailModal').css('display', 'none');
        } else {
            emailValid = false;
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

// 이메일 중복확인 클릭시
$(document).on('click', '#ckEmail', function () {
    let memberEmail = $('#modalEmail'); // 이메일 input 요소
    if (OriginalEmail !== memberEmail.val().trim()) {
        emailValid = false;

        // let regex = /^.*@.*/;
        let regex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

        if (regex.test(memberEmail.val().trim())) {
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
                data: { memberEmail: memberEmail.val().trim() },
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
                        $('#emailModal').css('display', 'flex'); // jQuery로 CSS 변경
                        authTime();
                    }
                },
                error: function (xhr, status, message) {},
            });
            // showSuccessAlert(memberEmail);
            return;
        } else {
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '이메일 형식이 올바르지 않습니다.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    memberEmail.focus();
                },
            });
        } // if문 종료
        // ajax로 이메일로 인증번호 전송하기.
    }
});

$('#modalAddress1').on('click', function () {
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

            document.getElementById('modalAddress1').value = roadAddr;
            addressInputs($('#modalAddress1').val().trim());
            $('#modalAddress1').prop('readonly', true);
        },
    }).open({
        left: window.screen.width / 2 - width / 2, // 화면 좌측 중앙 정렬
        top: window.screen.height / 2 - height / 2, // 화면 상단 중앙 정렬
        popupKey: 'daumApi0915', // key값을 활용하여 중복 팝업창 방지(아무 값이나 넣어도 됌)
    });
}

const deliveryYearSelect = $('#deliveryYearSelect');
const deliveryMonthSelect = $('#deliveryMonthSelect');

$(document).on('change', '#deliveryMonthSelect, #deliveryYearSelect', function () {
    const yearVal = deliveryYearSelect.val();
    const monthVal = deliveryMonthSelect.val();
    $('#deliveryFeeListBody').empty();
    getDeliveryFee(yearVal, monthVal);
    $('#riderDeliveryFeeModal').show();
});

$('#deliveryFeeModalShow').on('click', function () {
    $('#deliveryFeeListBody').empty();
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth();
    getDeliveryFee(year, month + 1);
    $('#riderDeliveryFeeModal').show();
});

// 배달비 내역 조회 (입금,정산)
function getDeliveryFee(year, month) {
    $.ajax({
        url: '/rider/ajaxSelectDeliveryFee',
        type: 'post',
        data: { year, month },
        success: function (response) {
            if (response.length != 0) {
                response.forEach((element) => {
                    $('#deliveryFeeListBody').append(substitute(element));
                });
            } else {
                $('#deliveryFeeListBody').empty().append(`
        <tr>
            <td colspan="6" class="text-center">배달 내역이 없습니다.</td>
        </tr>
    `);
            }
        },
        error: function (xhr, status, message) {},
    });
}

function setDeliverySelect() {
    // 실제로는 사용자 정보를 가져와서 여기서 year에 대입해주고 가입날짜를 넣어줘야함

    for (var i = memberYear; i <= nowYear; i++) {
        let option;
        if (i == nowYear) {
            option = '<option value="' + i + '"selected>' + i + '년</option>';
        } else {
            option = '<option value="' + i + '">' + i + '년</option>';
        }
        deliveryYearSelect.append(option);
    }
    for (var i = memberMonth; i <= nowMonth; i++) {
        let option;

        if (i == nowMonth) {
            option = '<option value="' + i + '" selected>' + i + '월</option>';
        } else {
            option = '<option value="' + i + '">' + i + '월</option>';
        }
        deliveryMonthSelect.append(option);
    }
}

function substitute(response) {
    return `
            <tr>
                <td>${response.feeType == 1 ? '배송비 입금' : '배송비 환전'}</td>
                <td>${response.feeAmount.toLocaleString()} 원</td>
                <td>${response.feeBalance.toLocaleString()} 원</td>
                 <td>${formatDate(response.createdAt)}</td>
            </tr>
    `;
}

function formatDate(dateStr) {
    const date = new Date(dateStr); // "2025-09-15T10:30:00" 같은 ISO 문자열 처리
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    const hh = String(date.getHours()).padStart(2, '0');
    const mi = String(date.getMinutes()).padStart(2, '0');
    const ss = String(date.getSeconds()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd} ${hh}:${mi}:${ss}`;
}

function createDeliveryYearSelect() {
    `
    <select name="deliveryYearSelect" id="deliveryYearSelect">
        <option value="2025">2025년</option>
    </select>
    `;
}

function createDeliveryMonthSelect() {
    `
    <select name="deliveryMonthSelect" id="deliveryMonthSelect">
        <option value="1">1월</option>
        <option value="2">2월</option>
        <option value="3">3월</option>
        <option value="4">4월</option>
        <option value="5">5월</option>
        <option value="6">6월</option>
        <option value="7">7월</option>
        <option value="8">8월</option>
        <option value="9">9월</option>
    </select>
    `;
}
