package com.ex.center.model.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.ex.center.model.data.ReasonDTO;

public interface ReasonRepository extends JpaRepository<ReasonDTO, Integer>{
    
}
