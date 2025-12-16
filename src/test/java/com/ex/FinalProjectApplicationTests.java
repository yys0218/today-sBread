package com.ex;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.ex.product.model.service.ProductService;

import lombok.RequiredArgsConstructor;

@SpringBootTest
@RequiredArgsConstructor
class FinalProjectApplicationTests {
	private final ProductService productService;

	@Test
	void contextLoads() {
		
		productService.getFileNames(6);
	}

}
