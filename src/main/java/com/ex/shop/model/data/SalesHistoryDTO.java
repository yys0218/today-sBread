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

@Entity
@Getter
@Setter
@Table(name = "sales_history")

//	판매자의 매출 관리에 활용되는 엔티티

/**
 * SalesHistoryDTO
 * ---------------------------
 * 판매자의 매출 정보를 저장하는 엔티티
 * 주문, 입금, 환불, 정산 등의 매출 내역을 관리
 */

public class SalesHistoryDTO {
	
     /**
     * 매출 번호 (PK)
     * 자동 생성되는 고유 번호
     */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_history_seq")
	@SequenceGenerator(name = "sales_history_seq", sequenceName = "sales_history_seq", allocationSize = 1)
	private Integer salesNo;				//	매출 번호
	
     /**
     * 주문 번호 (FK)
     * 어떤 주문이 매출로 기록되었는지 참조
     */
	@Column
	private int orderNo;				//	어떤 상품이 판매되었는지 참조
	
	 /**
     * 상점 번호 (FK)
     * 해당 매출이 발생한 상점 식별
     */
	@Column
	private int shopNo;					//	판매 가게 식별 번호
	
	 /**
     * 매출 유형
     * 1: 입금, 2: 환불, 3: 정산
     */
	@Column(nullable = false)
	private Integer salesType;				//	1: 입금 , 2: 환불 , 3: 정산
	
	 /**
     * 매출 금액
     * 실제 입금/환불/정산된 금액
     */
	@Column(nullable = false)
	private Integer salesAmount;			//	 매출 금액
	
    /**
     * 잔액
     * 매출 발생 후 남은 잔액
     */
	@Column
	private Integer salesBalance;			//	잔액
	
    /**
     * 등록 일시
     * 매출 내역이 기록된 날짜와 시간
     */
	@Column
	private LocalDateTime createdAt;		//	등록 일시 
	

//	@Column
//	private Integer orderStatus; 			// 1: 주문접수, 2: 배송진행, 3: 배송완료, 4: 주문거절

    /**
     * 주문 상태
     * 1: 주문접수, 2: 배송진행, 3: 배송완료, 4: 주문거절
     */
	@Column
	private Integer orderStatus; 			// 1: 주문접수, 2: 배송진행, 3: 배송완료, 4: 주문거절


	// ===== 추가 =====
	
    /**
     * 상품명
     * 어떤 상품에 대한 매출인지 표시
     * 최대 길이 255자
     */
    @Column(length = 255)
    private String productName;             // 상품명
    
    /**
     * 수량
     * 판매된 상품 수량
     */
    @Column
    private Integer quantity;               // 수량
	

//	public int getStatus() {
//    return (this.orderStatus != null) ? this.orderStatus : 0; // DB에서 가져온 orderStatus를 그대로 status로 사용
//}

    
    
    
    //	================================================
    // DB 컬럼(orderStatus)을 기반으로 한 상태값 반환용 메서드
    // 기존 코드나 서비스에서 getStatus()를 호출하고 있을 수 있으므로
    // 바로 삭제하지 않고 유지. 필요 시 getOrderStatus()로 대체 가능
    // 반환값: orderStatus가 null이면 0, 아니면 orderStatus 값 그대로
    // ================================================================    
	public int getStatus() {
    return (this.orderStatus != null) ? this.orderStatus : 0; // DB에서 가져온 orderStatus를 그대로 status로 사용
}
	
	// ==========================
	// 상태값 설정용 메서드 (빈 구현)
	// 현재는 아무 동작을 하지 않음
	// 기존 코드 호환성을 위해 남겨둠
	// 필요 시 로직 구현하거나 호출 코드 수정 가능
	// =============================================
	public void setStatus(int i) {				
	}

}
