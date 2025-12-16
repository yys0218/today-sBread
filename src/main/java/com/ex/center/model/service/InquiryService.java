package com.ex.center.model.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ex.DataNotFoundException;
import com.ex.center.model.data.InquiryDTO;
import com.ex.center.model.data.InquiryForm;
import com.ex.center.model.data.InquiryImg;
import com.ex.center.model.repository.InquiryImgRepository;
import com.ex.center.model.repository.InquiryRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryImgRepository inquiryImgRepository;

    // 리스트 출력
    public Page<InquiryDTO> inquiryList(int memberNo, Integer status, int page) {
        boolean all = (status == null || status == -1);
        List<Integer> statusList;
        if (all) {
            statusList = List.of(0, 1, 2, 3);
        } else if (status == 2) { // 완료(2,3)
            statusList = List.of(2, 3);
        } else if (status == 0) {
            statusList = List.of(0);
        } else if (status == 1) {
            statusList = List.of(1);
        } else {
            statusList = List.of(0, 1, 2, 3);
        }
        return inquiryRepository.inquiryList(memberNo, all, statusList, PageRequest.of(page, 10));
    }

    // 출력
    public InquiryDTO getInquiry(int inquiryNo) {
        Optional<InquiryDTO> _inquiry = this.inquiryRepository.findById(inquiryNo);
        if(_inquiry.isEmpty()){
            throw new DataNotFoundException("해당하는 문의글을 찾을 수 없습니다.");
        }
        InquiryDTO inquiry = _inquiry.get();
        List<InquiryImg> images = this.inquiryImgRepository.findByInquiry_InquiryNoOrderByFileOrderAsc(inquiryNo);
        inquiry.setInquiryImg(images);
        return inquiry;
    }

    // 이미지 출력
    public List<InquiryImg> getImages(int inquiryNo) {
        List<InquiryImg> images = this.inquiryImgRepository.findByInquiry_InquiryNoOrderByFileOrderAsc(inquiryNo);
        return images;
    }

    // 작성
    public InquiryDTO insertInquiry(int memberNo, InquiryForm form) {
        InquiryDTO inquiry = new InquiryDTO();
        inquiry.setMemberNo(memberNo);
        inquiry.setInquiryType(form.getInquiryType());
        inquiry.setInquiryTitle(form.getInquiryTitle());
        inquiry.setInquiryContent(form.getInquiryContent());
        // inquiry.setInquiryMail(form.getInquiryMail());
        inquiry.setInquiryStatus(0);
        this.inquiryRepository.save(inquiry);
        this.inquiryRepository.flush();

        if (form.getImages() != null) {
            int order = 0;

            // 프로젝트 루트 기준 경로
            Path rootPath = Paths.get("src/main/resources/static/image/inquiry").toAbsolutePath();
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

                    InquiryImg img = new InquiryImg();
                    img.setInquiry(inquiry);
                    img.setOriginFilename(file.getOriginalFilename());
                    img.setFilePath("/image/inquiry/" + saveName);
                    img.setFileOrder(order++);

                    // 연관관계 편의 메서드
                    if (inquiry.getInquiryImg() == null) {
                        inquiry.setInquiryImg(new ArrayList<>());
                    }
                    inquiry.getInquiryImg().add(img);

                    inquiryImgRepository.save(img);
                }
            }
        }
        return inquiry;
    }

    // 수정
    @Transactional
    public void updateInquiry(int inquiryNo, InquiryForm form, List<Integer> deleteImgIds) {
        Optional<InquiryDTO> _inquiry = this.inquiryRepository.findById(inquiryNo);
        if(_inquiry.isEmpty()){
            throw new DataNotFoundException("해당하는 문의를 찾을 수 없습니다.");
        }
        InquiryDTO inquiry = _inquiry.get();
        // 텍스트 수정
        inquiry.setInquiryType(form.getInquiryType());
        inquiry.setInquiryTitle(form.getInquiryTitle());
        inquiry.setInquiryContent(form.getInquiryContent());
        // inquiry.setInquiryMail(form.getInquiryMail());

        // 삭제 체크된 이미지 제거
        if (deleteImgIds != null && !deleteImgIds.isEmpty()) {
            for (Integer imgId : deleteImgIds) {
                inquiryImgRepository.findById(imgId).ifPresent(img -> {
                    if (img.getInquiry().getInquiryNo() == inquiryNo) {
                        try {
                            Path filePath = Paths.get("src/main/resources/static", img.getFilePath())
                                    .toAbsolutePath().normalize();
                            Files.deleteIfExists(filePath);
                        } catch (IOException e) {
                            throw new RuntimeException("기존 파일 삭제 실패", e);
                        }
                        inquiryImgRepository.delete(img);
                    }
                });
            }
        }

        if (form.getImages() != null) {
            int existingCount = inquiryImgRepository.findByInquiry_InquiryNoOrderByFileOrderAsc(inquiryNo).size();
            int order = existingCount;

            Path rootPath = Paths.get("src/main/resources/static/image/inquiry").toAbsolutePath();
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

                    InquiryImg img = new InquiryImg();
                    img.setInquiry(inquiry);
                    img.setOriginFilename(file.getOriginalFilename());
                    img.setFilePath("/image/inquiry/" + saveName);
                    img.setFileOrder(order++);

                    inquiryImgRepository.save(img);
                }
            }
        }
        inquiryRepository.save(inquiry);
    }

    public void deleteInquiry(int inquiryNo) {
        this.inquiryRepository.deleteById(inquiryNo);
    }

    public void inquiryReply(String inquiryReply, int num){
        Optional<InquiryDTO> _inquiry = this.inquiryRepository.findById(num);
        if(_inquiry.isEmpty()){
            throw new DataNotFoundException("해당하는 문의를 찾을 수 없습니다.");
        }
        InquiryDTO inquiry = _inquiry.get();
        inquiry.setInquiryReply(inquiryReply);
        inquiry.setInquiryStatus(1);
        inquiry.setInquiryReplyAt(LocalDateTime.now());
    }

    public Page<InquiryDTO> adminInquiryList(int inquiryType, int inquiryStatus, String sortField, String sortDir, int page, int size) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.DESC, "inquiryAt"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return inquiryRepository.searchInquiryList(inquiryType, inquiryStatus, pageable);
    }
}
