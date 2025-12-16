package com.ex.alert.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ex.alert.model.data.AlertHistoryDTO;

@Repository                                  
public interface AlertHistoryRepository extends JpaRepository<AlertHistoryDTO, Integer> {

}
