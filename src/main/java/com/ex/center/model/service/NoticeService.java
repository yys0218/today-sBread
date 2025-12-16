package com.ex.center.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.ex.DataNotFoundException;
import com.ex.center.model.data.NoticeDTO;
import com.ex.center.model.data.NoticeForm;
import com.ex.center.model.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    
    private final NoticeRepository noticeRepository;

    // 메인 페이지 공지사항(3개) 출력
    public List<NoticeDTO> noticeMainList(){
        List<NoticeDTO> notices = this.noticeRepository.noticeMainList();
        return notices;
    }

    // 공지사항 목록(list)(검색가능) 출력
    // 매개변수 : 검색어 kw , 카테고리 category , 페이지 번호 page
    public Page<NoticeDTO> listNotice(String kw, String category, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("noticeNo"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.noticeRepository.listNotice(kw, category, pageable);
    }

    // 공지사항 본문(detail) 출력 - 매개변수 : 글번호(num)
    public NoticeDTO getNotice(int num){
        Optional<NoticeDTO> notice = this.noticeRepository.findById(num);
        if(notice.isPresent()){
            return notice.get();
        }else{
            throw new DataNotFoundException("해당 글을 찾을 수 없습니다.");
        }
    }
    
    // 공지사항 글 작성(insert) - 관리자만 가능
    public NoticeDTO insertNotice(NoticeForm form) {
        NoticeDTO notice = new NoticeDTO();
        notice.setNoticeCategory(form.getCategory());
        notice.setNoticeTitle(form.getTitle());
        notice.setNoticeContent(form.getContent());
        notice.setNoticeStatus(0);
        this.noticeRepository.save(notice);
        return notice;
    }

    // 공지사항 글 수정(update) - 관리자만 가능 동일한 폼 사용
    public void updateNotice(int noticeNo, NoticeForm form) {
        Optional<NoticeDTO> _notice = this.noticeRepository.findById(noticeNo);
        if(_notice.isEmpty()){throw new DataNotFoundException("해당 글을 찾을 수 없습니다.");}
        NoticeDTO notice = _notice.get();
        notice.setNoticeCategory(form.getCategory());
        notice.setNoticeTitle(form.getTitle());
        notice.setNoticeContent(form.getContent());
        this.noticeRepository.save(notice);
    }

    // 해당 공지사항 글 상태 변경 - 관리자만 가능
    public void cancelNotice(int num) {
        Optional<NoticeDTO> _notice = this.noticeRepository.findById(num);
        if(_notice.isEmpty()){
            throw new DataNotFoundException("해당하는 공지사항을 찾을 수 없습니다.");
        }
        NoticeDTO notice = _notice.get();
        int status = notice.getNoticeStatus();
        if (status > 0) {
            notice.setNoticeStatus(0);
        } else {
            notice.setNoticeStatus(1);
        }
        this.noticeRepository.save(notice);
    }

    // 공지 삭제
    public void deleteNotice(int num) {
        Optional<NoticeDTO> _notice = this.noticeRepository.findById(num);
        if(_notice.isEmpty()){
            throw new DataNotFoundException("해당하는 공지사항을 찾을 수 없습니다.");
        }
        NoticeDTO notice = _notice.get();
        this.noticeRepository.delete(notice);
    }

    // 고객 센터 검색결과
    public List<NoticeDTO> search(String kw){
        List<NoticeDTO> list = this.noticeRepository.search(kw);
        return list;
    }
}
