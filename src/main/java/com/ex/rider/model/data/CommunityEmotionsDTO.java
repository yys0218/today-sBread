package com.ex.rider.model.data;

import java.time.LocalDateTime;

import com.ex.member.model.data.MemberDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "community_Emotions")
public class CommunityEmotionsDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "community_emotions_seq")
    @SequenceGenerator(name = "community_emotions_seq", sequenceName = "community_emotions_seq", allocationSize = 1)
    private int emotionsNo;

    @ManyToOne
    private RiderCommunityDTO community;

    @ManyToOne
    private MemberDTO member;

    private int emotionsType; // 1 : 좋아요 / 2: 싫어요

    private LocalDateTime createdAt;
}
