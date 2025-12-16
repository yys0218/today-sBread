package com.ex.main.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ex.member.model.data.MemberDTO;
import com.ex.product.model.data.ProductDTO;
import com.ex.product.model.service.ProductService;
import com.ex.shop.model.repository.ShopRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/*") //http://localhost:8080/으로 접속 시 매핑
public class MainController {
	
	private final ProductService productService;
	private final ShopRepository shopRepository;
	
	//메인 접속: http://localhost:8080/ 
	@GetMapping("/") 	//뷰로 전달: 인기빵 리스트, 최신빵 리스트, 로그인한 멤버 번호
	public String main(HttpSession session, Model model ) {
		//로그인한 사용자라면 MemberNo 뷰로 전송
		MemberDTO member = (MemberDTO)session.getAttribute("user");
		if(member!=null) { //로그인한 사용자라면
			model.addAttribute("memberNo",member.getMemberNo()); //memberNo 뷰로 전달
		}
		
		//인기 순위 리스트 출력
		/*
		 * List<ProductDTO> pList = productService.popularList(); //전체 지역구 인기 순위 리스트
		 * 
		 * //찜여부 저장 for(ProductDTO dto : pList) { //상품꺼내기 //찜 여부 저장 int isWished = 0;
		 * //찜 아님 if( member!=null && productService.isWished(member.getMemberNo(),
		 * dto.getProductNo()) ) { //로그인되어있고, 찜이 되어있다면 isWished = 1; //찜 되어있음 }
		 * dto.setIsWished(isWished); }
		 */
	
		/*
		 * ArrayList<ProductDTO> nList = productService.newList(); //신상 등록 리스트 //출력용
		 * 상품마다 shopName저장 for(ProductDTO dto : nList) { //상품꺼내기 //찜 여부 저장 int isWished
		 * = 0; //찜 아님 if( member!=null && productService.isWished(member.getMemberNo(),
		 * dto.getProductNo()) ) { //로그인되어있고, 찜이 되어있다면 isWished = 1; //찜 되어있음 }
		 * dto.setIsWished(isWished); }
		 */
		//시(도) 목록 전달
		List<String> sidoList = shopRepository.findDistinctSido();
		model.addAttribute("sidoList",sidoList);
		
		//뷰로 전달
		//model.addAttribute("popularList", pList); //인기빵 리스트
		//model.addAttribute("newList", nList); //최신빵 리스트
		return "/main/main";
	}
	
	//시/군/구 목록 요청시
	@GetMapping("/sigungu")
	@ResponseBody
	public List<String> ajaxSigunguList(@RequestParam("sido") String sido){
		//시/군/구 목록 전달
		return shopRepository.findDistinctSigungu(sido);
	}

	//시/군/구에 해당하는 인기 목록 반환
	@GetMapping("/sigunguPopularList")
	//@ResponseBody								//매개변수: 시도, 시군구
	public String ajaxSigunguPopular( @RequestParam("sido") String sido, @RequestParam("sigungu") String sigungu, HttpSession session, Model model){
		
		//로그인한 사용자라면 MemberNo 뷰로 전송
		MemberDTO member = (MemberDTO)session.getAttribute("user");
		if(member!=null) { //로그인한 사용자라면
			model.addAttribute("memberNo",member.getMemberNo()); //memberNo 뷰로 전달
		}
		
		//시군구 인기 리스트
		List<ProductDTO> pList = productService.sigunguPopularList(sigungu);	
		//찜여부 저장
		for(ProductDTO dto : pList) { //상품꺼내기			
			int isWished = 0; //찜 아님
			if( member!=null && productService.isWished(member.getMemberNo(), dto.getProductNo()) ) { //로그인되어있고, 찜이 되어있다면
				isWished = 1; //찜 되어있음
			}
		dto.setIsWished(isWished);
		}
			
		//뷰로 전달
		model.addAttribute("popularList", pList); //인기빵 리스트
		
		//다시 조회한 결과를 fragment로 리턴
		return "main/sigungu-product :: sigunguPopularList";
	}	
	
	//시/군/구에 해당하는 최신 목록 반환
	@GetMapping("/sigunguNewList")
	//@ResponseBody								//매개변수: 시도, 시군구
	public String ajaxSigunguNew( @RequestParam("sido") String sido, @RequestParam("sigungu") String sigungu, HttpSession session, Model model){
		
		//로그인한 사용자라면 MemberNo 뷰로 전송
		MemberDTO member = (MemberDTO)session.getAttribute("user");
		if(member!=null) { //로그인한 사용자라면
			model.addAttribute("memberNo",member.getMemberNo()); //memberNo 뷰로 전달
		}
	
		List<ProductDTO> nList = productService.sigunguNewList(sigungu); //시군구 신상 리스트
		//출력용 상품마다 shopName저장
		for(ProductDTO dto : nList) { //상품꺼내기				
			//찜 여부 저장
			int isWished = 0; //찜 아님
			if( member!=null && productService.isWished(member.getMemberNo(), dto.getProductNo()) ) { //로그인되어있고, 찜이 되어있다면
				isWished = 1; //찜 되어있음
			}
			dto.setIsWished(isWished);
		}
		
		//뷰로 전달
		model.addAttribute("newList", nList); //최신빵 리스트	
		
		//다시 조회한 결과를 fragment로 리턴
		return "main/sigungu-product :: sigunguNewList";
	}
		
	
	//동/도로명 목록 요청시
	@GetMapping("/bname")
	@ResponseBody
	public List<String> ajaxBnameList(@RequestParam("sido") String sido, @RequestParam("sigungu") String sigungu){
		//동/도로명 목록 전달
		return shopRepository.findDistinctBname(sido, sigungu);
	}
	
	//사용자 위치 정보 세션에 저장
	@PostMapping("/setLocation")
	@ResponseBody
	public void setLocation(HttpSession session, @RequestBody Map<String, String> location) {
		session.setAttribute("location", location);
	}
	
	//사용자 위치 정보 세션에서 삭제
	@GetMapping("/deleteLocation")
	@ResponseBody
	public void deleteLocation(HttpSession session) {
		//세션에서 location 삭제
		session.removeAttribute("location");
	}
}
