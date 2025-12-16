package com.ex.admin.model.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ex.DataNotFoundException;
import com.ex.admin.model.repository.AdminShopRepository;
import com.ex.center.model.data.InquiryDTO;
import com.ex.center.model.data.ReportDTO;
import com.ex.center.model.repository.InquiryRepository;
import com.ex.center.model.repository.ReportRepository;
import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.shop.model.data.ShopDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminShopRepository adminShopRepository;
    private final InquiryRepository inquiryRepository;
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    /** MemberNo 로 MemberDTO 꺼내기 */
    public MemberDTO findById(int memberNo) {
        Optional<MemberDTO> _member = this.memberRepository.findById(memberNo);
        if (_member.isEmpty()) {
            throw new DataNotFoundException("해당하는 유저를 찾을 수 없습니다.");
        }
        MemberDTO member = _member.get();
        return member;
    }

    public MemberDTO findByMemberID(String memberId) {
        Optional<MemberDTO> _member = this.memberRepository.findByMemberId(memberId);
        if (_member.isEmpty()) {
            throw new DataNotFoundException("해당하는 유저를 찾을 수 없습니다.");
        }
        MemberDTO member = _member.get();
        return member;
    }

    /**
     * 문의글 전체조회
     * status = -1: 철회, 0: 미답변, 1:답변, 2:답변확인
     */
    public Page<InquiryDTO> adminInquiryList(int status, int type, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        // 전체
        if (status == 2) {
            if (type == -1) {
                return inquiryRepository.findAll(pageable);
            } else {
                return inquiryRepository.findByInquiryType(type, pageable);
            }
        }
        // 철회만
        else if (status == -1) {
            return (type == -1)
                    ? inquiryRepository.findByInquiryStatus(-1, pageable)
                    : inquiryRepository.findByInquiryStatusAndInquiryType(-1, type, pageable);
        }
        // 미답변만
        else if (status == 0) {
            return (type == -1)
                    ? inquiryRepository.findByInquiryStatus(0, pageable)
                    : inquiryRepository.findByInquiryStatusAndInquiryType(0, type, pageable);
        }
        // 답변(= DB의 1,2)
        else if (status == 1) {
            return (type == -1)
                    ? inquiryRepository.findByInquiryStatusIn(List.of(1, 2), pageable)
                    : inquiryRepository.findByInquiryStatusInAndInquiryType(List.of(1, 2), type, pageable);
        }

        // 기본은 전체
        return inquiryRepository.findAll(pageable);
    }

    /**
     * 신고글 전체조회
     * status = -1 : 철회, 0 : 답변전, 1: 답변후, 2: 답변확인
     */
    public Page<ReportDTO> adminReportList(int status, int type, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);

        // 전체
        if (status == 2) {
            if (type == -1) {
                return reportRepository.findAll(pageable);
            } else {
                return reportRepository.findByReportType(type, pageable);
            }
        }
        // 철회만
        else if (status == -1) {
            return (type == -1)
                    ? reportRepository.findByReportStatus(-1, pageable)
                    : reportRepository.findByReportStatusAndReportType(-1, type, pageable);
        }
        // 미답변만
        else if (status == 0) {
            return (type == -1)
                    ? reportRepository.findByReportStatus(0, pageable)
                    : reportRepository.findByReportStatusAndReportType(0, type, pageable);
        }
        // 답변(= DB의 1,2)
        else if (status == 1) {
            return (type == -1)
                    ? reportRepository.findByReportStatusIn(List.of(1, 2), pageable)
                    : reportRepository.findByReportStatusInAndReportType(List.of(1, 2), type, pageable);
        }

        // 기본은 전체
        return reportRepository.findAll(pageable);
    }

    /**
     * 입점신청 글 전체조회
     * 결과 : null / Y / N / C
     */
    // public Page<ShopDTO> adminShopList(String result, int page, int size) {
    //     boolean all = (result == null || result.isBlank());

    //     List<String> resultList;

    //     if (all) {
    //         // 전체: 모든 결과 포함
    //         resultList = List.of("C", "N", "Y");
    //     } else if ("A".equals(result)) {
    //         // 미답변: result 값이 null 인 것만 조회
    //         resultList = List.of(""); // 또는 DB에서 null 처리 필요시 Collections.emptyList()
    //     } else {
    //         // 승인(Y), 거절(N), 자진철회(C)
    //         resultList = List.of(result);
    //     }

    //     return this.adminBoardRepository.adminShopList(all, result, resultList, PageRequest.of(page, size));
    // }

    // 입점신청 상세
    public ShopDTO getShop(int shopNo) {
        Optional<ShopDTO> _shop = this.adminShopRepository.findById(shopNo);
        if (_shop.isEmpty()) {
            throw new DataNotFoundException("해당하는 매장을 찾을 수 없습니다.");
        }
        ShopDTO shop = _shop.get();
        return shop;
    }

    // 승인
    @Transactional
    public void approveRegistry(int shopNo) {
        Optional<ShopDTO> _shop = this.adminShopRepository.findById(shopNo);
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

    // 거절
    @Transactional
    public void refuseRegistry(int shopNo, int shopRegReason) {
        Optional<ShopDTO> _shop = this.adminShopRepository.findById(shopNo);
        if (_shop.isEmpty()) {
            throw new DataNotFoundException("해당하는 상점을 찾을 수 없습니다.");
        }
        ShopDTO shop = _shop.get();
        shop.setShopRegReason(shopRegReason);
        shop.setShopRegResult("N");
        shop.setShopCreatedAt(LocalDateTime.now());
        this.adminShopRepository.save(shop);
    }

    // 폐점 현황 전체조회
    // 시간으로 조회
    public Page<ShopDTO> adminClosingList(LocalDateTime startDate, int page) {
        return this.adminShopRepository.adminClosingList(startDate, PageRequest.of(page, 10));
    }
}
