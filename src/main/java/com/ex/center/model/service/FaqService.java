package com.ex.center.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.DataNotFoundException;
import com.ex.center.model.data.FaqDTO;
import com.ex.center.model.data.FaqForm;
import com.ex.center.model.repository.FaqRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    // FAQ 리스트 출력
    public Page<FaqDTO> faqList(String kw, String category, int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("faqNo"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.faqRepository.listFaq(kw, category, pageable);
    }

    // FAQ 글 하나 가져오기
    public FaqDTO getFaq(int num){
        Optional<FaqDTO> _faq = this.faqRepository.findById(num);
        if(_faq.isEmpty()){throw new DataNotFoundException("해당하는 문의글을 찾을 수 없습니다.");}
        FaqDTO faq = _faq.get();
        return faq;
    }

    // FAQ 글 작성(insert) - 관리자만 가능
    public void insertFaq(FaqForm form) {
        FaqDTO faq = new FaqDTO();
        faq.setFaqCategory(form.getCategory());
        faq.setFaqTitle(form.getTitle());
        faq.setFaqContent(form.getContent());
        faq.setFaqStatus(0);
        faq.setReadCount(0);
        this.faqRepository.save(faq);
    }

    // FAQ 글 수정(update) - 관리자만 가능 동일한 폼 사용
    public void updateFaq(int faqNo, FaqForm form) {
        Optional<FaqDTO> _faq = this.faqRepository.findById(faqNo);
        if(_faq.isEmpty()){throw new DataNotFoundException("해당하는 문의글을 찾을 수 없습니다.");}
        FaqDTO faq = _faq.get();
        faq.setFaqCategory(form.getCategory());
        faq.setFaqTitle(form.getTitle());
        faq.setFaqContent(form.getContent());
        this.faqRepository.save(faq);
    }

    // 고객 센터 검색결과
    public List<FaqDTO> search(String kw) {
        List<FaqDTO> list = this.faqRepository.search(kw);
        return list;
    }

    // 메인 페이지 FAQ 출력
    public List<FaqDTO> faqMainList(String category) {
        return this.faqRepository.faqMainList(category);
    }

    // 세션당 조회수 1회 증가
    @Transactional
    public void increaseReadCountOnce(int faqNo, HttpSession session) {
        String key = "faq_viewed_" + faqNo;
        if (session.getAttribute(key) == null) {
            faqRepository.increaseReadCount(faqNo);
            session.setAttribute(key, true);
        }
    }

    // Faq 글 숨김처리
    public void cancelFaq(int faqNo){
        Optional<FaqDTO> _faq = this.faqRepository.findById(faqNo);
        if(_faq.isEmpty()){
            throw new DataNotFoundException("해당하는 FAQ를 찾을 수 없습니다.");
        }
        FaqDTO faq = _faq.get();
        int status = faq.getFaqStatus();
        if (status > 0) {
            faq.setFaqStatus(0);
        } else {
            faq.setFaqStatus(1);
        }
        this.faqRepository.save(faq);
    }

    // faq 글 삭제
    public void deleteFaq(int faqNo){
        Optional<FaqDTO> _faq = this.faqRepository.findById(faqNo);
        if(_faq.isEmpty()){
            throw new DataNotFoundException("해당하는 FAQ를 찾을 수 없습니다.");
        }
        FaqDTO faq = _faq.get();
        this.faqRepository.delete(faq);
    }

}
