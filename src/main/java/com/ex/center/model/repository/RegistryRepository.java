package com.ex.center.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ex.shop.model.data.ShopDTO;

public interface RegistryRepository extends JpaRepository<ShopDTO, Integer>{
    
    Optional<ShopDTO> findTopByMemberNoOrderByShopRegAtDesc(int memberNo);
    int countByMemberNoAndShopRegResultIsNull(int memberNo);
}
