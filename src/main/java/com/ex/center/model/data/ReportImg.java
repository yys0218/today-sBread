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
@Table(name="report_img")
public class ReportImg {
    //CREATE SEQUENCE report_img_seq START WITH 1 INCREMENT BY 1;
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_img_seq_gen")
	@SequenceGenerator(name = "report_img_seq_gen", sequenceName = "report_img_seq", allocationSize = 1)
    private int reportImgNo;

    @ManyToOne
    @JoinColumn(name = "report_no")
    private ReportDTO report;

    private String originFilename;
    
    private String filePath;

    private int fileSize;

    private int fileOrder;
}
