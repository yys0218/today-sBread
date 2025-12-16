// 모든 jQuery AJAX 요청에 CSRF 헤더 자동 첨부
$(function () {
    const token = $('meta[name="_csrf"]').attr('content');
    const header = $('meta[name="_csrf_header"]').attr('content');
    $(document).ajaxSend(function (e, xhr) {
        if (token && header) xhr.setRequestHeader(header, token);
    });
});

$('#pwToggleIcon').on('click', function () {
    let pwInput = $('#password');
    let pwToggle = $('#pwToggleIcon');
    if (pwInput.attr('type') == 'password') {
        pwInput.attr('type', 'text');
        $(this).removeClass('bi-eye-slash-fill').addClass('bi-eye-fill');
    } else {
        pwInput.attr('type', 'password');
        pwToggle.removeClass('bi-eye-fill').addClass('bi-eye-slash-fill');
    }
});

// 로그인 버튼 클릭
$('.btn-sub').on('click', function () {
    let idElement = $('#riderId');
    let id = idElement.val().trim();
    let pwElement = $('#password');
    let pw = pwElement.val().trim();
    if (!id || !pw) {
        if (!id) {
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '아이디를 입력해 주세요.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    idElement.focus();
                },
            });
            return false;
        } else {
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: ' 비밀번호를 입력해 주세요.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    pwElement.focus();
                },
            });
            return false;
        }
        return;
    }
    $(this).closest('form').submit();
});

//
// 아이디 찾기 모달 열기
$('#findRiderId').on('click', function () {
    $('.dimmed, #modal-find-id').show();
    $('.modal_title').children().eq(1).show();
});

// 모달 닫기 버튼
$('.close-btn').on('click', function () {
    closeIdModal();
});

// 모달 배경 클릭 시 닫기
$('.dimmed').on('click', function () {
    closeIdModal();
});

// 모달 ESC 키 닫기
$(document).on('keydown', function (e) {
    if (e.key === 'Escape') closeIdModal();
});

// 모달 ID 찾기 버튼
$('#modal_search_id').on('click', function () {
    let name = $('#modal_id_name').val().trim();
    let email = $('#modal_id_email').val().trim();
    if (!name || !email) {
        if (!name) {
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '이름을 입력해 주세요.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    $('#modal_id_name').focus();
                },
            });
        } else if (!email) {
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '이메일를 입력해 주세요.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    $('#modal_id_email').focus();
                },
            });
        }
        return;
    }

    const box = $(this).closest('.modal_box');
    const content = box.find('.modal_content');
    const hidden = box.find('.hidden_content');
    const result = box.find('#result_input_id');
    console.log('실행');
    // Ajax 로 실제 조회 후 성공시 값 주입
    $.ajax({
        url: '/rider/ajaxFindRiderId',
        type: 'POST',
        data: { riderName: $('#modal_id_name').val(), riderEmail: $('#modal_id_email').val() },
        success: function (response) {
            console.log('response : ' + response);
            if (response) {
                result.val(response);
                // 화면 전환
                content.hide();
                hidden.show();
                $('.modal_title').children().eq(1).hide();
                // 확인 버튼 표시(레이아웃 유지)
                $('#id_insert_btn').css('display', 'inline-flex');

                // 필요 시 기본 버튼 그룹 숨김
                $('.btn-group1').hide();
                // Ajax 성공시 끝
            } else {
                // Ajax 실패시
                // 이름과 이메일 입력창 비워줌
                $('#modal_id_name').val('');
                $('#modal_id_email').val('');
                showErrorTitleAlert('아이디 찾기 실패', '아이디 또는 비밀번호를 입력해주세요.');
                // Ajax 실패시 끝
            }
        },
        error: function (xhr, status, message) {},
    });
});

// 확인 버튼 (결과 반영)
$('#id_insert_btn').on('click', function () {
    let result = $('#result_input_id');
    let input = $('#riderId');
    console.log(input);
    if (result.val().trim()) {
        input.val(result.val().trim());
        closeIdModal();
    }
});

// 모달 닫기 + 초기화
function closeIdModal() {
    // 입력값 초기화
    // 아이디 찾기 모달
    $('#modal_id_name').val('');
    $('#modal_id_email').val('');
    $('#result_input_id').val('');

    //비밀번호 찾기 모달
    $('#modal_pw_id').val('');
    $('#modal_pw_email').val('');
    // 영역/버튼 상태 초기화
    $('.modal_content').show();
    $('.hidden_content').hide();
    $('#id_insert_btn').hide();
    $('.btn-group1').show();

    // 모달/배경 닫기
    $('.modal_layer, .dimmed').hide();
}
// 아이디 찾기 모달 끝

// 비밀번호 찾기 모달 시작
$('#findRiderPw').on('click', function () {
    $('.dimmed, #modal-find-pw').show();
    $('.modal_title').children().eq(1).show();
});

$('#modal_search_pw').on('click', function () {
    let id = $('#modal_pw_id').val().trim();
    let email = $('#modal_pw_email').val().trim();
    if (!id || !email) {
        if (!id) {
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '아이디를 입력해 주세요.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    $('#modal_pw_id').focus();
                },
            });
        } else if (!email) {
            Swal.fire({
                icon: 'error', // 오류 아이콘
                title: '입력 오류', // 알림 제목
                text: '이메일를 입력해 주세요.', // 알림 메시지
                confirmButtonText: '확인', // 버튼 내용
                didClose: () => {
                    $('#modal_pw_email').focus();
                },
            });
        }
    } else {
        // ajax로 아이디와 이메일로 정보 조회 후 일치하는 계정이 있을 경우 임시비밀번호 생성 및 메일로 전달
        console.log('아이디 & email 둘다 값 있음');
        console.log('id : ' + id);
        console.log('email : ' + email);
        $.ajax({
            url: '/rider/ajaxFindRiderPw',
            type: 'POST',
            data: { memberId: id, memberEmail: email },
            success: function (response) {
                console.log(response);
                switch (response) {
                    case 'success':
                        showSuccessTitleAlert('성공', '해당 이메일로 임시비밀번호를 전송했습니다.');
                        break;
                    case 'memberNotFound':
                        showErrorTitleAlert('오류', '아이디 또는 이메일을 확인해주세요.');
                        break;
                    case 'mailSendError':
                        showErrorTitleAlert('오류', '이메일 전송에 실패했습니다 다시 시도해주세요.');
                        break;
                    default:
                        break;
                }
            },
            error: function (xhr, status, message) {},
        });
    }
});
