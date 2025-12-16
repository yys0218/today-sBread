package com.ex.product.model.data;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_REVIEW_COMMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReviewCommentDTO {

    // 리뷰 번호 (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REVIEW_COMMENT_NO")
    private Integer reviewCommentNo;

    // 상점 번호
    @Column(name = "REVIEW_NO", nullable = false)
    private Long reviewNo;

    // 리뷰 내용
    @Column(name = "CONTENT", columnDefinition = "VARCHAR2(255)", nullable = false)
    private String content;

    private LocalDateTime createdAt;

}
