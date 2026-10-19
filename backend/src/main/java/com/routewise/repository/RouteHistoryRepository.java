package com.routewise.repository;

import com.routewise.entity.RouteHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteHistoryRepository extends JpaRepository<RouteHistory, Long> {
    List<RouteHistory> findByUserIdOrderByRecordedAtDesc(Long userId);
    Page<RouteHistory> findByUserIdOrderByRecordedAtDesc(Long userId, Pageable pageable);
    List<RouteHistory> findByRouteIdOrderByRecordedAtDesc(Long routeId);
}
