package com.ex.shop.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ex.product.model.data.ProductReviewDTO;

public interface ShopReviewRepository extends JpaRepository<ProductReviewDTO, Integer> {
    List<ProductReviewDTO> findByShopNo(int shopNo); // shopNo 기준으로 리뷰 조회
    
    @Query("SELECT COUNT(r) FROM ProductReviewDTO r WHERE r.shopNo = :shopNo")
    int countReviewsByShopNo(@Param("shopNo") int shopNo);

    @Query("SELECT COALESCE(AVG(r.rating),0) FROM ProductReviewDTO r WHERE r.shopNo = :shopNo")
    double avgRatingByShopNo(@Param("shopNo") int shopNo);
}

