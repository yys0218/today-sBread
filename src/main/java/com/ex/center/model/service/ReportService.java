package com.ex.center.model.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ex.DataNotFoundException;
import com.ex.admin.model.repository.AdminShopRepository;
import com.ex.center.model.data.ReportDTO;
import com.ex.center.model.data.ReportForm;
import com.ex.center.model.data.ReportImg;
import com.ex.center.model.repository.ReportImgRepository;
import com.ex.center.model.repository.ReportRepository;
import com.ex.member.model.repository.MemberRepository;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.ShopRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportImgRepository reportImgRepository;
    private final MemberRepository memberRepository;
    private final AdminShopRepository adminShopRepository;

    // 리스트 출력
    public Page<ReportDTO> reportList(int memberNo, Integer status, int page) {
        boolean all = (status == null || status == -1);
        List<Integer> statusList;
        if (all) {
            statusList = List.of(0, 1, 2);
        } else if (status == 0) {
            statusList = List.of(0);
        } else if (status == 1) {
            statusList = List.of(1, 2);
        } else {
            statusList = List.of(0, 1, 2);
        }
        return reportRepository.reportList(memberNo, all, statusList, PageRequest.of(page, 10));
    }

    // 출력
    public ReportDTO getReport(int reportNo) {
        Optional<ReportDTO> _report = this.reportRepository.findById(reportNo);
        if (_report.isEmpty()) {
            throw new DataNotFoundException("");
        }
        ReportDTO report = _report.get();
        List<ReportImg> images = this.reportImgRepository.findByReport_ReportNoOrderByFileOrderAsc(reportNo);
        report.setReportImg(images);
        return report;
    }

    // 이미지 출력
    public List<ReportImg> getImages(int reportNo) {
        List<ReportImg> images = this.reportImgRepository.findByReport_ReportNoOrderByFileOrderAsc(reportNo);
        return images;
    }

    // 작성
    public ReportDTO insertReport(int memberNo, ReportForm form) {
        ReportDTO report = new ReportDTO();
        report.setMemberNo(memberNo);
        report.setReportType(form.getReportType());
        report.setReportReason(form.getReportReason());
        report.setReportRef(form.getReportRef());
        report.setReportTitle(form.getReportTitle());
        report.setReportContent(form.getReportContent());
        report.setReportStatus(0);
        // report.setReportMail(form.getReportMail());
        if (form.getReportEtc() != null) {
            report.setReportEtc(form.getReportEtc());
        }
        this.reportRepository.save(report);
        this.reportRepository.flush();

        if (form.getImages() != null) {
            int order = 0;

            // 프로젝트 루트 기준 경로
            Path rootPath = Paths.get("src/main/resources/static/image/report").toAbsolutePath();
            try {
                Files.createDirectories(rootPath);
            } catch (IOException e) {
                throw new RuntimeException("디렉토리 생성 실패", e);
            }

            for (MultipartFile file : form.getImages()) {
                if (!file.isEmpty()) {
                    String uuid = UUID.randomUUID().toString();
                    String ext = Optional.ofNullable(file.getOriginalFilename())
                            .filter(f -> f.contains("."))
                            .map(f -> f.substring(f.lastIndexOf(".")))
                            .orElse("");
                    String saveName = uuid + ext;

                    Path savePath = rootPath.resolve(saveName);
                    try {
                        file.transferTo(savePath.toFile());
                    } catch (IOException e) {
                        throw new RuntimeException("파일 저장 실패", e);
                    }

                    ReportImg img = new ReportImg();
                    img.setReport(report);
                    img.setOriginFilename(file.getOriginalFilename());
                    img.setFilePath("/image/report/" + saveName);
                    img.setFileOrder(order++);

                    // 연관관계 편의 메서드
                    if (report.getReportImg() == null) {
                        report.setReportImg(new ArrayList<>());
                    }
                    report.getReportImg().add(img);

                    reportImgRepository.save(img);
                }
            }
        }
        return report;
    }

    // 수정
    @Transactional
    public void updateReport(int reportNo, ReportForm form, List<Integer> deleteImgIds) {
        Optional<ReportDTO> _report = this.reportRepository.findById(reportNo);
        if (_report.isEmpty()) {
            throw new DataNotFoundException("");
        }
        ReportDTO report = _report.get();

        // 텍스트 수정
        report.setReportType(form.getReportType());
        report.setReportTitle(form.getReportTitle());
        report.setReportReason(form.getReportReason());
        report.setReportRef(form.getReportRef());
        report.setReportContent(form.getReportContent());
        if (form.getReportEtc() != null) {
            report.setReportEtc(form.getReportEtc());
        }
        // report.setReportMail(form.getReportMail());

        // 삭제 체크된 이미지 제거
        if (deleteImgIds != null && !deleteImgIds.isEmpty()) {
            for (Integer imgId : deleteImgIds) {
                reportImgRepository.findById(imgId).ifPresent(img -> {
                    if (img.getReport().getReportNo() == reportNo) {
                        try {
                            Path filePath = Paths.get("src/main/resources/static", img.getFilePath())
                                    .toAbsolutePath().normalize();
                            Files.deleteIfExists(filePath);
                        } catch (IOException e) {
                            throw new RuntimeException("기존 파일 삭제 실패", e);
                        }
                        reportImgRepository.delete(img);
                    }
                });
            }
        }

        if (form.getImages() != null) {
            int existingCount = reportImgRepository.findByReport_ReportNoOrderByFileOrderAsc(reportNo).size();
            int order = existingCount;

            Path rootPath = Paths.get("src/main/resources/static/image/report").toAbsolutePath();
            try {
                Files.createDirectories(rootPath);
            } catch (IOException e) {
                throw new RuntimeException("디렉토리 생성 실패", e);
            }

            for (MultipartFile file : form.getImages()) {
                if (!file.isEmpty() && order < 5) { // ← 5장 제한 추가
                    String uuid = UUID.randomUUID().toString();
                    String ext = Optional.ofNullable(file.getOriginalFilename())
                            .filter(f -> f.contains("."))
                            .map(f -> f.substring(f.lastIndexOf(".")))
                            .orElse("");
                    String saveName = uuid + ext;

                    Path savePath = rootPath.resolve(saveName);
                    try {
                        file.transferTo(savePath.toFile());
                    } catch (IOException e) {
                        throw new RuntimeException("파일 저장 실패", e);
                    }

                    ReportImg img = new ReportImg();
                    img.setReport(report);
                    img.setOriginFilename(file.getOriginalFilename());
                    img.setFilePath("/image/report/" + saveName);
                    img.setFileOrder(order++);

                    reportImgRepository.save(img);
                }
            }
        }

        reportRepository.save(report);
    }

    public void deleteReport(int reportNo) {
        this.reportRepository.deleteById(reportNo);
    }

    public void reportReply(String reportReply, int num) {
        Optional<ReportDTO> _report = this.reportRepository.findById(num);
        if (_report.isEmpty()) {
            throw new DataNotFoundException("해당하는 문의를 찾을 수 없습니다.");
        }
        ReportDTO report = _report.get();
        report.setReportReply(reportReply);
        report.setReportStatus(1);
        report.setReportReplyAt(LocalDateTime.now());
    }

    public Page<ReportDTO> adminReportList(int reportType, int reportStatus, String sortField, String sortDir, int page, int size) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.DESC, "reportAt"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return reportRepository.searchReportList(reportType, reportStatus, pageable);
    }

    // 신고대상 조회
    public String checkReportRef(String reportRef) {
        // 회원 닉네임 먼저 확인
        if (memberRepository.findByMemberNick(reportRef).isPresent()) {return "MEMBER";}
        // 상점 이름 확인
        if (adminShopRepository.findByShopName(reportRef).isPresent()) {return "SHOP";}
        // 둘 다 아님
        return "NONE";
    }
}
