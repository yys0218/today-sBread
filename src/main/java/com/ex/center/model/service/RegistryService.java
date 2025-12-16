package com.ex.center.model.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ex.DataNotFoundException;
import com.ex.center.model.data.RegistryForm;
import com.ex.center.model.repository.RegistryRepository;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.rider.model.service.KakaoLocalService;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.ShopRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistryService {

    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;
    private final RegistryRepository registryRepository;

    private final KakaoLocalService kakaoLocalService; // 카카오 로컬 서비스 ( 주소 -> 좌표 변환 )

    public Page<ShopDTO> myRegistryList(int memberNo, int page) {
        return shopRepository.findMyPendingOrRejected(memberNo, PageRequest.of(page, 10));
    }

    public ShopDTO getShop(int shopNo) {
        Optional<ShopDTO> _shop = this.shopRepository.findById(shopNo);
        if (_shop.isEmpty()) {
            throw new DataNotFoundException("해당하는 상점을 찾을 수 없습니다.");
        }
        ShopDTO shop = _shop.get();
        return shop;
    }

    /** MemberNo 로 해당 유저가 마지막으로 쓴 입점신청 출력 
     *  return : null or ShopDTO
     */
    public ShopDTO getLastShop(int memberNo){
        Optional<ShopDTO> _shop = this.registryRepository.findTopByMemberNoOrderByShopRegAtDesc(memberNo);
        if(_shop.isEmpty()){
            return null;
        }
        return _shop.get();
    }

    public ShopDTO applyRegistry(int memberNo, RegistryForm form) {
        ShopDTO s = new ShopDTO();
        s.setMemberNo(memberNo);
        s.setTinNo(form.getTinNo());
        s.setBusinessName(form.getBusinessName());
        s.setBusinessOpenAt(form.getBusinessOpenAt());
        s.setBusinessContact(form.getBusinessContact());
        s.setBusinessMail(form.getBusinessMail());
        s.setBusinessBank(form.getBusinessBank());
        s.setBusinessAccName(form.getBusinessAccName());
        s.setBusinessAccNum(form.getBusinessAccNum());
        s.setShopRegResult(null);
        s.setDeliveryFee(form.getDeliveryFee());
        s.setShopRegAt(LocalDateTime.now());
        s.setOpenTime(form.getOpenTime());
        s.setCloseTime(form.getCloseTime());
        s.setShopName(form.getShopName());
        s.setShopContact(form.getShopContact());
        s.setShopInfo(form.getShopInfo());
        s.setShopAddress(form.getShopRoadAdd() + " " + form.getShopDetailAdd());
        s.setShopSido(form.getShopSido());
        s.setShopSigungu(form.getShopSigungu());
        s.setShopBname(form.getShopBname());
        Map<String, Double> map = this.kakaoLocalService.getCoordinates(s.getShopAddress());
        s.setLatitude(map.get("latitude"));
        s.setLongitude(map.get("longitude"));

        return shopRepository.save(s);
    }

    public void dummyRegistryShop() {
        String[] bizNumbers = {
                "8231047629", "9152038471", "7205981342", "6582019475", "3041975826",
                "4820619753", "9271034856", "1502983746", "8310294756", "7642091835",
                "5293810476", "6938275104", "2085713496", "8753201946", "9138205476",
                "2401938576", "3748291056", "6051983742", "8357291046", "4920831756",
                "9172035841", "5820391746", "7362819045", "8492017356", "6102839475",
                "1958374026", "4729103856", "9031827456", "2609481735", "4150488054"
        };
        // 가게 이름 더미 (20개)
        String[] shopNames = {
                "행운동 단팥빵", "신림 바게트", "봉천 케이크하우스", "관악 크로와상",
                "낙성대 파리바게트", "서울대 도넛하우스", "신원동 브런치카페", "신림역 식빵연구소",
                "보라매 호두과자", "은천동 파이샵", "난향동 와플스토어", "신사동 머핀팩토리",
                "조원동 식빵공작소", "신림 치즈케이크", "인헌동 마카롱하우스", "남현동 크림빵",
                "서원동 바게트마을", "대학동 카스테라", "청룡동 베이커리", "삼성동 쿠키샵"
        };

        // 가게 소개 문구 더미 (20개)
        String[] shopInfos = {
                "매일 아침 갓 구운 빵을 제공합니다.",
                "천연 발효종으로 만든 건강한 빵집입니다.",
                "케이크 전문점, 예약 환영합니다.",
                "동네에서 가장 맛있는 크로와상을 구워냅니다.",
                "유기농 밀가루와 버터만 사용합니다.",
                "아이들과 함께 즐길 수 있는 디저트 카페.",
                "30년 전통의 수제빵집입니다.",
                "커피와 잘 어울리는 빵을 준비했어요.",
                "매일매일 다른 신메뉴가 나오는 빵집.",
                "건강을 생각한 통밀빵 전문점.",
                "달콤한 디저트로 하루를 행복하게!",
                "부드러운 크림빵이 인기 메뉴입니다.",
                "제과제빵 자격증 소지자가 직접 운영합니다.",
                "케이터링, 단체 주문도 가능합니다.",
                "주말에는 특별한 케이크를 선보입니다.",
                "버터향 가득한 페이스트리를 맛보세요.",
                "지역 주민이 사랑하는 동네 빵집입니다.",
                "이웃과 함께하는 따뜻한 공간.",
                "전통과 현대가 어우러진 디저트샵.",
                "매장 내 포토존도 준비되어 있습니다."
        };

        // 사업자명 (20개)
        String[] businessNames = {
                "행운제과", "신림제빵소", "봉천베이커리", "관악케이크샵",
                "낙성대빵집", "서울대빵공방", "신원브레드", "신림식빵",
                "보라매제과", "은천파이샵", "난향와플", "신사머핀",
                "조원식빵", "신림치즈케익", "인헌마카롱", "남현크림하우스",
                "서원바게트", "대학카스테라", "청룡디저트", "삼성쿠키"
        };

        // 은행명 (20개 예시)
        String[] banks = {
                "국민은행", "신한은행", "우리은행", "하나은행", "IBK기업은행",
                "농협은행", "SC제일은행", "케이뱅크", "카카오뱅크", "토스뱅크",
                "국민은행", "신한은행", "우리은행", "하나은행", "IBK기업은행",
                "농협은행", "SC제일은행", "케이뱅크", "카카오뱅크", "토스뱅크"
        };

        String[] extraAddresses = {
                "서울특별시 관악구 남부순환로 1546",
                "서울특별시 관악구 봉천로 167",
                "서울특별시 관악구 남부순환로 1427",
                "서울특별시 관악구 남부순환로 1914",
                "서울특별시 관악구 봉천로 415-1",
                "서울특별시 관악구 봉천로 279-7",
                "서울특별시 관악구 낙성대로 32",
                "서울특별시 관악구 난곡로9길 11",
                "서울특별시 관악구 남부순환로151길 55",
                "서울특별시 관악구 남부순환로155길 29",
                "서울특별시 관악구 관천로 105",
                "서울특별시 관악구 문성로 187",
                "서울특별시 관악구 신림로11길 89-18",
                "서울특별시 관악구 대학7길 26",
                "서울특별시 관악구 서림3길 15",
                "서울특별시 관악구 청림6길 3",
                "서울특별시 관악구 관악로30길 27",
                "서울특별시 관악구 남부순환로228길 6",
                "서울특별시 관악구 은천로 137",
                "서울특별시 관악구 은천로 143"
        };
        String[] accNames = {
                "홍길동", "김철수", "이영희", "박민수", "최지현",
                "정우성", "한가인", "이승민", "강민호", "조은지",
                "윤하늘", "장서윤", "서지호", "임도현", "오수빈",
                "권태영", "신유진", "백승훈", "하은별", "차민석"
        };

        for (int i = 0; i < bizNumbers.length; i++) {

            ShopDTO s = new ShopDTO();
            s.setMemberNo(i + 20);
            s.setTinNo(bizNumbers[i]);
            s.setBusinessName(businessNames[i]); // 사업자명 (필수)
            s.setBusinessOpenAt(LocalDate.of(2010 + (i % 10), (i % 12) + 1, (i % 28) + 1)); // 개업일자 (필수)
            s.setBusinessContact("02-8" + String.format("%03d-%04d", i, i + 1000)); // 사업자 전화번호 (필수)
            s.setBusinessMail("biz" + i + 20 + "@example.com"); // 사업자 이메일
            s.setBusinessBank(banks[i]); // 사업자 은행
            s.setBusinessAccName(accNames[i]); // 예금주명
            s.setBusinessAccNum("100-" + (100000 + i)); // 계좌번호
            s.setShopRegResult(null); // 승인(Y) 비승인(N) (초기 null) s
            s.setShopRegAt(LocalDateTime.now());
            s.setShopName(shopNames[i]); // 빵 상점 이름
            s.setShopContact("02-8" + String.format("%03d-%04d", i + 100, i + 2000));
            s.setShopInfo(shopInfos[i]); // 가게 소개 문구
            s.setShopAddress(extraAddresses[i]); // 가게 주소
            // ✅ 주소 파싱 (공백으로 나눔 → [서울시, 관악구, 은천로, 43])
            String[] parts = extraAddresses[i].split(" ");

            if (parts.length >= 2) {
                s.setShopSido(parts[0]); // 서울시
                s.setShopSigungu(parts[1]); // 관악구
            }
            if (parts.length >= 3) {
                s.setShopBname(parts[2]); // 은천로 (도로명, 동 이름 등)
            }
            Map<String, Double> map = this.kakaoLocalService.getCoordinates(s.getShopAddress());
            s.setLatitude(map.get("X"));
            s.setLongitude(map.get("Y"));

            shopRepository.save(s);
        }
    }

    // 승낙
    @Transactional
    public void approveRegistry(int shopNo) {
        Optional<ShopDTO> _shop = this.shopRepository.findById(shopNo);
        if (_shop.isEmpty()) {
            throw new DataNotFoundException("해당하는 상점을 찾을 수 없습니다.");
        }
        ShopDTO shop = _shop.get();
        int memberNo = shop.getMemberNo();
        MemberDTO member = this.memberRepository.findByMemberNo(memberNo);
        member.setMemberRole(1);
        shop.setShopRegResult("Y");
        shop.setShopCreatedAt(LocalDateTime.now());
        shop.setShopStatus(0);
    }

    public void cancelRegistry(int shopNo) {
        Optional<ShopDTO> _shop = this.shopRepository.findById(shopNo);
        if (_shop.isEmpty()) {
            throw new DataNotFoundException("해당하는 상점을 찾을 수 없습니다.");
        }
        ShopDTO shop = _shop.get();
        shop.setShopRegResult("C");
        this.shopRepository.save(shop);
    }

    // true : 입점대기중 , false : 입점신청내역 없음
    public boolean applyCheck(MemberDTO user){
        int count = this.registryRepository.countByMemberNoAndShopRegResultIsNull(user.getMemberNo());
        boolean result = count!=0;
        return result;
    }
}
