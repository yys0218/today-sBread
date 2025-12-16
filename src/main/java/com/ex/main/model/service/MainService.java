package com.ex.main.model.service;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import com.ex.main.model.data.CategoryDTO;
import com.ex.main.model.repository.MainRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MainService {
	private final MainRepository mainRepository;

	public List<CategoryDTO> getCategory() {
		return mainRepository.findAll();
	}
	
	// ===== 작업자 : 안성진 =====
	// [헤더 1차 카테고리용] 대분류만 가져오기
	// MainRepository : findTopCats() 사용 (depth=0, status=0)
	// 기대값 : 빵 / 케이크 / 샌드위치/샐러드 / 디저트 순서대로 (categoryNo asc)
	public List<CategoryDTO> getTopCats(){
		return mainRepository.findTopCats();
	}
	
	// [하단 소분류 바] 특정 대분류 클릭 시 자식(소분류) 목록 가져오기
	// MainRepository : findSubCats(parentCode) 사용 (status=0)
	// 파라미터 예시
	// parentCode = "br"	-> 빵 하위 (식빵/간식빵/건강빵/도넛/페스츄리/파이)
	// parentCode = "cake"	-> 케이크 하위 (미니/롤, 1호, 2호)
	public List<CategoryDTO> getSubCats(String parentCode){
		return mainRepository.findSubCats(parentCode);
	}
	// ===== 안성진 작업 완료 =====
}
