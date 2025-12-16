package com.ex.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration //자원을 저장?
public class WebConfig implements WebMvcConfigurer{
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		//요청이 들어오면 다음 경로에서 찾음
		//썸머노트 파일 업로드
		registry.addResourceHandler("/upload/productInfo/**").addResourceLocations("file:" + System.getProperty("user.dir") + "\\src\\main\\resources\\static\\upload\\productInfo");
		//썸네일 포함 상품사진 업로드
		registry.addResourceHandler("/upload/product/**").addResourceLocations("file:" + System.getProperty("user.dir") + "\\src\\main\\resources\\static\\upload\\product\\");
	}
}
