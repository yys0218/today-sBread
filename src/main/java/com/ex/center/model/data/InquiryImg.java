package com.ex.center.model.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name="inquiry_img")
public class InquiryImg {
    //CREATE SEQUENCE inquiry_img_seq START WITH 1 INCREMENT BY 1;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inquiry_img_seq_gen")
	@SequenceGenerator(name = "inquiry_img_seq_gen", sequenceName = "inquiry_img_seq", allocationSize = 1)
    private int inquiryImgNo;

    @ManyToOne
    @JoinColumn(name = "inquiry_no")
    private InquiryDTO inquiry;

    private String originFilename;
    
    private String filePath;

    private int fileOrder;
}
