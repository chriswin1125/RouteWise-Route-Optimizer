package com.routewise.repository;

import com.routewise.entity.DeliveryOrder;
import com.routewise.entity.DeliveryPriority;
import com.routewise.entity.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryOrderRepository extends JpaRepository<DeliveryOrder, Long> {

    List<DeliveryOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<DeliveryOrder> findByIdAndUserId(Long id, Long userId);

    List<DeliveryOrder> findByIdInAndUserId(List<Long> ids, Long userId);

    @Query("SELECT d FROM DeliveryOrder d WHERE d.user.id = :userId " +
           "AND (:status IS NULL OR d.status = :status) " +
           "AND (:priority IS NULL OR d.priority = :priority) " +
           "AND (:query IS NULL OR LOWER(d.customerName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "     OR LOWER(d.address) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<DeliveryOrder> searchDeliveries(
            @Param("userId") Long userId,
            @Param("status") DeliveryStatus status,
            @Param("priority") DeliveryPriority priority,
            @Param("query") String query,
            Pageable pageable
    );

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, DeliveryStatus status);

    List<DeliveryOrder> findByUserIdAndStatus(Long userId, DeliveryStatus status);
}
