package com.ex.main.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.main.model.data.CategoryDTO;

@Repository
public interface MainRepository extends JpaRepository<CategoryDTO, Integer>{
	
	//테이블의 모든 레코드 조회
	List<CategoryDTO> findAll();
	
	// ===== 작업자 : 안성진 =====
	// 대분류 + 노출 -> 헤더 1차 카테고리 (DEPTH=0, STATUS=0)
	@Query("select c from CategoryDTO c where c.depth=0 and c.status=0 order by c.categoryNo asc")
	List<CategoryDTO> findTopCats();
	
	// 소분류 + 노출 -> 대분류 클릭 시 하단 가로바 (부모코드 매칭, STATUS=0)
	@Query("select c from CategoryDTO c where c.parentCode= :parent and c.status=0 order by c.categoryNo asc")
	List<CategoryDTO> findSubCats(@Param("parent") String parentCode);
	// ===== 안성진 작업 완료 =====

}