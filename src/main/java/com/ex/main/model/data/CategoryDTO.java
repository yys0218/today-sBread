package com.ex.main.model.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="category") //테이블 이름 지정
public class CategoryDTO {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "category_seq")
	@SequenceGenerator(name = "category_seq", sequenceName = "category_seq", allocationSize = 1)
	private int categoryNo;
	
	@Column(length=100)
	private String parentCode;
	
	@Column(length=100)
	private String categoryCode;
	
	@Column(length=100)
	private String categoryName;
	
	private int depth;
	
	@Column(columnDefinition = "Number default 0")
	private int status;
	
}
