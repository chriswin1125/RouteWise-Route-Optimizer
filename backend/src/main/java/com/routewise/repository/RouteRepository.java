package com.routewise.repository;

import com.routewise.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    List<Route> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<Route> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Route> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(r.distanceSavedKm), 0.0) FROM Route r WHERE r.user.id = :userId")
    Double sumDistanceSavedByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(AVG(r.savingsPercentage), 0.0) FROM Route r WHERE r.user.id = :userId")
    Double avgSavingsPercentageByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Route r WHERE r.user.id = :userId " +
           "AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Route> searchRoutes(@Param("userId") Long userId, @Param("name") String name, Pageable pageable);
}
