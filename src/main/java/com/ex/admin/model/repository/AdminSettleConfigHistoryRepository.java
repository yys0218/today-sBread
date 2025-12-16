package com.ex.admin.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ex.admin.model.data.SettleConfigHistory;

public interface AdminSettleConfigHistoryRepository extends JpaRepository<SettleConfigHistory, Integer> {
    Page<SettleConfigHistory> findAllByOrderByHistoryIdDesc(Pageable pageable);

    SettleConfigHistory findTopByOrderByUpdatedAtDesc();
}
