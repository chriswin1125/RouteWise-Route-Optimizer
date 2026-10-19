package com.routewise.repository;

import com.routewise.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    List<RouteStop> findByRouteIdOrderByStopSequenceAsc(Long routeId);
    void deleteByRouteId(Long routeId);
}
