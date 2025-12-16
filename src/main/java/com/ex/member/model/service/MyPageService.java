package com.ex.member.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ex.member.model.data.MemberDTO;
import com.ex.member.model.repository.MemberRepository;
import com.ex.order.model.data.OrderHistoryDTO;
import com.ex.order.model.data.ShoppingCartDTO;
import com.ex.order.model.repository.OrderHistoryRepository;
import com.ex.order.model.repository.ShoppingCartRepository;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.data.QnaDTO;
import com.ex.product.model.data.WishDTO;
import com.ex.product.model.repository.QnaMapper;
import com.ex.product.model.repository.WishMapper;
import com.ex.product.model.service.ProductService;
import com.ex.product.model.service.QnaService.QnaPage;
import com.ex.shop.model.data.ShopDTO;
import com.ex.shop.model.repository.ShopRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final OrderHistoryRepository orderHistoryRepository;
	private final WishMapper wishMapper;
	private final ShoppingCartRepository shoppingCartRepository;
	private final MemberRepository memberRepository;
	private final ShopRepository shopRepository;
	private final ProductService productService;
	private final QnaMapper qnaMapper;
    
	// 주문 내역
	public List<OrderHistoryDTO> getOrderHistory(MemberDTO member){
		return orderHistoryRepository.findByMemberOrderByOrderNoDesc(member);
	}
	
	// 찜 목록
	@Transactional(readOnly = true)
	public List<WishDTO> getMemberWishPage(MemberDTO member, int page, int size){
		int safePage = Math.max(page, 1);
		int safeSize = Math.min(Math.max(size, 1), 50);
		int offset = (safePage - 1) * safeSize;
		return wishMapper.selectMemberWishList(member.getMemberNo(), offset, safeSize);
	}
	
	// 내 찜 총 개수
	public int countMemberWishList(MemberDTO member) {
		return wishMapper.countMemberWishList(member.getMemberNo());
	}
	
	// 단건 삭제
	@Transactional
	public boolean deleteWishOne(MemberDTO member, int wishNo) {
		return wishMapper.deleteByWishNo(member.getMemberNo(), wishNo) > 0;
	}
	
	// 선택 삭제 (체크박스 일괄)
	@Transactional
	public int deleteWishBulk(MemberDTO member, List<Integer> wishNos) {
		if(wishNos == null || wishNos.isEmpty()) return 0;
		return wishMapper.deleteByWishNos(member.getMemberNo(), wishNos);
	}
	
	// 찜목록에서 장바구니 담기
	@Transactional
	public void addToCart(int memberNo, int productNo, int qty) {
		int safeQty = Math.max(1, qty);
		
		// 회원/상품 조회
		MemberDTO member = memberRepository.findByMemberNo(memberNo);
		if(member == null) throw new IllegalArgumentException("회원 없음: " + memberNo);
		ProductDTO product = productService.product(productNo);
		if(product == null) throw new IllegalArgumentException("상품 없음: " + productNo);
		
		// 같은 회원 + 상품 장바구니 존재 여부
		Optional<ShoppingCartDTO> existingOpt = shoppingCartRepository.findByMemberAndProductNo(member, productNo);
		
		if(existingOpt.isPresent()) {
			ShoppingCartDTO cart = existingOpt.get();
			cart.setQuantity(cart.getQuantity() + safeQty);
			shoppingCartRepository.save(cart);
			return;
		}
		
		ShopDTO shop = shopRepository.findByShopNo(product.getShopNo());
		if(shop == null) throw new IllegalStateException("상품의 상점 없음 : " + product.getShopNo());
		
		ShoppingCartDTO cart = new ShoppingCartDTO();
		cart.setMember(member);
		cart.setShop(shop);
		cart.setProductNo(productNo);
		cart.setProductName(product.getProductName());
		cart.setPrice(product.getPrice());
		cart.setQuantity(safeQty);
		cart.setThumbnailName(product.getThumbnailName());
		cart.setCartReg(java.time.LocalDateTime.now());
		
		shoppingCartRepository.save(cart);
	}
	
	// 마이페이지 - 상품 문의 목록
	// memberNo, page, size, status, q
	public List<QnaDTO> findMyQna(int memberNo, int page, int size, String status, String q){
		int safePage = Math.max(page, 1);
		int safeSize = Math.min(Math.max(size, 1), 50);
		int offset = (safePage - 1) * safeSize;
		
		return qnaMapper.findMyQna(memberNo, offset, safeSize, status, q);
	}
	
	// 내 QnA 페이지 (마이페이지용)
	// memberNo, page, size, 검색(q), 상태(status: WAIT/DONE/ALL)
	@Transactional(readOnly = true)
	public QnaPage getMyQnaPage(int memberNo, int page, int size, String q, String status) {
	    int p = Math.max(1, page);
	    int s = Math.max(1, size);
	    int offset = (p - 1) * s;

	    List<QnaDTO> items = qnaMapper.findMyQna(memberNo, offset, s, status, q);
	    int total = qnaMapper.countMyQna(memberNo, status, q);

	    return new QnaPage(items, total, p, s);
	}


}