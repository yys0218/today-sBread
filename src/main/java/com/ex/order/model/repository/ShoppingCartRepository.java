package com.ex.order.model.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ex.member.model.data.MemberDTO;
import com.ex.order.model.data.ShoppingCartDTO;
import com.ex.shop.model.data.ShopDTO;


@Repository // Service에서 사용할 메서드(쿼리문) 선언
public interface ShoppingCartRepository extends JpaRepository<ShoppingCartDTO, Integer> {


	 //장바구니 목록 조회 = "select * from shopping_cart where buyer_No = ?"
	 //매개변수: MemberDTO member객체
	 ArrayList<ShoppingCartDTO> findByMember(MemberDTO member);
	 
	 //내 장바구니 목록에서 추가하려는 productNo와 일치하는 장바구니 레코드가 있으면 조회
	 //매개변수 : productNo
	 Optional<ShoppingCartDTO> findByProductNoAndMember(int productNo,MemberDTO member);
	
	 // 장바구니에서 선택되어 주문에 출력될 상품 조회 ="select * from shopping_cart where buyer_No =? and cart_No = ?"
	 // 매개변수: memberDTO, cartNo
	 ShoppingCartDTO findByMemberAndCartNo(MemberDTO member, int cartNo);
	 
	 //장바구니의 가장 최신 장바구니 번호 조회(바로 '구매' 클릭한 상품)
	 //매개변수: memberDTO
	 @Query("select max(cartNo) from ShoppingCartDTO where member=:member order By cartReg desc")
	 Optional<Integer> getCartNoByMember(@Param("member") MemberDTO member);
	 
	 //장바구니의 shop (내역) 조회
	 //매개변수 : memberDTO
	 @Query("select shop from ShoppingCartDTO where member=:member group by shop")
	 ArrayList<ShopDTO> findByMemberGroupByShop(@Param("member") MemberDTO member);
	 
	 //shop과 member로 장바구니 (내역)조회
	 //매개변수 : memberDTO, shopDTO
	 ArrayList<ShoppingCartDTO> findByShopAndMember(ShopDTO shop, MemberDTO member);
	 
	 // 작업자 : 안성진
	 // "찜 목록"에서 장바구니 담기를 누를 때 쓰는 메서드
	 // 같은 회원의 같은 상품이 이미 장바구니에 있는지 조회
	 Optional<ShoppingCartDTO> findByMemberAndProductNo(MemberDTO member, int productNo);
}
