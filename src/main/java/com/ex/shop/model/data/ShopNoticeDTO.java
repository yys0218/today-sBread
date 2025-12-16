package com.ex.shop.model.data;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * ShopNoticeDTO
 * ---------------------------
 * 판매자 페이지 알림 정보를 관리하는 엔티티
 * 공지, 알림 제목과 내용, 고정 여부 등을 포함
 */

@Entity
@Getter
@Setter
@Table(name="shop_Notice")

public class ShopNoticeDTO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopNoticeNo_seq")
	@SequenceGenerator(name = "shopNoticeNo_seq" , sequenceName = "shopNoticeNo_seq", allocationSize = 1)
	private int shopNoticeNo;		//	판매자 페이지 알림 개수 
	
	/** 알림 제목 */
	@Column(length = 200)		
	private String shopNoticeTitle;			//	판매자페이지 알림사항 제목
	
	/** 알림사항 내용 */
	@Column(length = 1000)
	private String shopNoticeContent;		//	판매자페이지 알림사항 내용
	
	/** 판매자 페이지 알림사항 */
	@Column
	private String shopAlert;				//	판매자 페이지 알림 사항 
	
	/** 상점 번호 */
	@Column
	private Integer shopNo;  				// 상점 번호
	
	/** 등록 일시 */
	@Column
	private LocalDateTime createdAt;		//	등록 일시 
	
	/** 공지 고정 여부 */
	@Column
	private boolean pinned; 				// 공지 고정 여부
	
	
	
}
