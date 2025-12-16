//위치 정보 사용

//현재 사용자의 위치 좌표를 가져오는 함수 (promise 생성)
function getUserPosition() {
	//const lat = 0;
	//const lng = 0;
	//리턴값을 promise로 래핑
	return new Promise((resolve, reject) => { //성공,실패 시
		//위치좌표 가져옴
		navigator.geolocation.getCurrentPosition(
			(position) => resolve({ //성공 시 promise가 리턴할 값
				lat: position.coords.latitude, //현재 위도 
				lng: position.coords.longitude //현재 경도
			}),
			//실패 시 promise가 리턴할 값
			(error) => reject(error), //실패 시 코드에 따른 행동은 사용하는 곳에서 지정하기
			//옵션 설정
			({
				timeout: 5000, //위치 정보 승인 요청의 대기 시간(밀리초)-5초
				enableHighAccuracy: false, //고정밀도 위치요청을 캐치할 것인지
				maximumAge: 0 //위치 정보의 캐시 기한(밀리초) - 캐시된 위치 정보 사용안함=항상 최신 위치 정보
			})
		);
	});
}

//현재 좌표를 주소로 변환하는 함수 (promise 생성)
function getAddress(lat, lng) {
	return new Promise((resolve, reject) => {
		//const sido = null;
		//const sigungu = null;
		//주소-좌표 변환 객체 생성
		let geocoder = new kakao.maps.services.Geocoder();
		//coord={전체주소, 시도, 시군구}
		let coord = new kakao.maps.LatLng(lat, lng);
		//변환
		geocoder.coord2Address(coord.getLng(), coord.getLat(), function(result, status) {
			//변환 성공 시
			if (status === kakao.maps.services.Status.OK) {
				//전체주소 addr 변환
				const addr = result[0].road_address || result[0].address;
				//addr에서 sido, sigungu, bname 얻기
				resolve({
					sido: addr.region_1depth_name,
					sigungu: addr.region_2depth_name
				});
			} else {
				reject('주소 변환 실패');
			}
		});
	});
}


//시군구 목록 가져오는 함수 - 
function getSigungu(){
	//시군구 목록 요청
	$.ajax({
		url : '/sigungu',
		method : 'get',
		data : { sido : $('#sido').val() },
		success: function(sigunguList){
			//성공 응답의 시군구 리스트 반복해서 꺼내기
			sigunguList.forEach(function(sigungu){
				$('#sigungu').append(new Option(sigungu, sigungu));			
			});
		},
		error : function(){
			showToast("다시 시도해주세요.","error");
			//알림이 사라진 후(2초 후) 자동 새로고침 되도록 설정
			setTimeout(function(){location.reload();},2000);
		}
	});
}
	

	/*		
	//세션에 저장된 위치정보 삭제
	$('.deleteLocation').on('click',function(){
		//세션에 저장된 값이 있는지 확인
		//if(){	
			showConfirmAlert(
		        '내 위치정보가 삭제됩니다.', // 표시할 메시지
		        () => {// 사용자가 '예'를 눌렀을 때 실행
		    		$.ajax({
		    			url : '/deleteLocation',
		    			success : function(){
				        	showToast('위치정보가 삭제되었습니다.', 'success');
				        	//기본값으로 변경(관악구)
				        	sidoDefault();
		    			},
		    			error : function(){
				        	showToast('다시 시도해주세요.', 'error');
		    			}
		    		});
		        	
		        },
		        () => {// 사용자가 '아니오'를 눌렀을 때 실행
		          console.log('취소 클릭됨');
		          showToast('삭제가 취소되었습니다.', 'error');
		        }
		      );
			//값 초기화
			sidoDefault(); 
		//}		
	});
	
			//사용자 위치정보 반영
			$('.user_location').on('click',function(){
		navigator.geolocation.getCurrentPosition(
			(position) => { //위치 정보 가져오기 성공 시(콜백 인자=Position객체)
				let lat = position.coords.latitude; //현재 위도
				let lng = position.coords.longitude; //현재 경도
				//좌표를 주소로 변환&sido, sigungu, bname적용
				getAddr(lat, lng);
				//지역에 맞게 제목, 목록 수정하기
				changeSigungu();
			}, 
			(error) => { //위치 정보 가져오기 에러 시(콜백인자 = PositionError객체)
				switch(error.code){ //에러.코드에 따라 다르게 처리 가능
				case error.PERMISSION_DENIED:
					console.log("사용자 승인 거부");
					showToast("기본 위치로 지정됩니다.","warning");
					sidoDefault();
					//changeSigungu();
				break;
				case error.POSITION_UNAVAILABLE:
					console.log("위치 정보를 사용할 수 없음");
					showToast("기본 위치로 지정됩니다.","warning");
					sidoDefault();
					//changeSigungu();
				break;
				case error.TIMEOUT:
					console.log("승인 요청 시간 초과"); //대기 시간 기본값은 무한
					showToast("다시 시도해주세요.","warning");
				break;
				case error.UNKNOWN_ERROR:
					console.log("알 수 없는 오류 발생");
					showToast("다시 시도해주세요.","warning");
				break;
				}
				//(기본 위치로) 한번에 선언하는게 낫지 changeSigungu보다 
				//sidoDefault();
			},
			{	//옵션 설정
				timeout: 5000, //위치 정보 승인 요청의 대기 시간(밀리초)-5초
				enableHighAccuracy: false, //고정밀도 위치요청을 캐치할 것인지
				maximumAge: 0 //위치 정보의 캐시 기한(밀리초) - 캐시된 위치 정보 사용안함=항상 최신 위치 정보
			}
		);
	});
	
		//좌표를 주소로 변환하는 함수
	function getAddr(lat, lng){
		//주소-좌표 변환 객체 생성
		let geocoder = new kakao.maps.services.Geocoder();
		
		//변환
		let coord = new kakao.maps.LatLng(lat, lng);
		let callback = function(result, status){
			if(status === kakao.maps.services.Status.OK){				
				//전체주소 addr 변환
				const addr = result[0].road_address || result[0].address;
				//addr에서 sido, sigungu, bname 얻기
				const sido = addr.region_1depth_name;
				const sigungu = addr.region_2depth_name;
				const bname = addr.region_3depth_name;				
				//주소를 select선택창에 적용하기
				$('#sido').empty().append(new Option(sido,sido));
				$('#sigungu').empty().append(new Option(sigungu,sigungu)); 
				$('#bname').empty().append(new Option(bname,bname));
				//사용자 위치 정보 세션에 저장하기
				$.ajax({
					url : '/setLocation',
					method : 'post',
					contentType : 'application/json',
					data : JSON.stringify({ sido, sigungu, bname }),
					success : function(){
						console.log('성공');
					}
				});
			}
		}
		geocoder.coord2Address(coord.getLng(), coord.getLat(), callback);
	}	
	
	
	//동/도로명 기존 값 비우기
	//$('#bname').empty().append(new Option("전체",""));
	
	//동/도로명 목록 요청
	$.ajax({
		url : '/bname',
		method : 'get',
		data : {
			//시도, 시군구 값 전달
			sido : $('#sido').val(),
			sigungu : $(this).val()
		},
		success: function(bnameList){
			//성공 응답의 동도로명 리스트 반복해서 꺼내기
			bnameList.forEach(function(bname){
				$('#bname').append(new Option(bname, bname));			
			});
		},
		error : function(){
			showToast("다시 시도해주세요.","error");
			//알림이 사라진 후(2초 후) 자동 새로고침 되도록 설정
			setTimeout(function(){
				location.reload();
			},2000);
		}
	});
*/


	//동도로명 변경 시
	//$('#bname').on('change',function(){
		//제목 변경
	//	$('.bname').empty().text($('#bname').val());
		//목록 변경
		
	//});