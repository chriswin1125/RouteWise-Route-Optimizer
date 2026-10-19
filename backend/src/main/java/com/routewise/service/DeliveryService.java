package com.routewise.service;

import com.routewise.dto.common.PagedResponse;
import com.routewise.dto.delivery.DeliveryOrderRequest;
import com.routewise.dto.delivery.DeliveryOrderResponse;
import com.routewise.dto.delivery.DeliveryStatusUpdateRequest;
import com.routewise.entity.DeliveryOrder;
import com.routewise.entity.DeliveryPriority;
import com.routewise.entity.DeliveryStatus;
import com.routewise.entity.User;
import com.routewise.exception.ResourceNotFoundException;
import com.routewise.exception.UnauthorizedAccessException;
import com.routewise.mapper.EntityDtoMapper;
import com.routewise.repository.DeliveryOrderRepository;
import com.routewise.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final DeliveryOrderRepository deliveryOrderRepository;
    private final UserRepository userRepository;
    private final EntityDtoMapper mapper;

    public DeliveryService(
            DeliveryOrderRepository deliveryOrderRepository,
            UserRepository userRepository,
            EntityDtoMapper mapper
    ) {
        this.deliveryOrderRepository = deliveryOrderRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public DeliveryOrderResponse createDelivery(Long userId, DeliveryOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        DeliveryOrder order = new DeliveryOrder();
        order.setUser(user);
        order.setCustomerName(request.customerName().trim());
        order.setAddress(request.address().trim());
        order.setLatitude(request.latitude());
        order.setLongitude(request.longitude());
        order.setContactPhone(request.contactPhone());
        order.setNotes(request.notes());
        order.setPriority(request.priority() != null ? request.priority() : DeliveryPriority.MEDIUM);
        order.setStatus(request.status() != null ? request.status() : DeliveryStatus.PENDING);

        DeliveryOrder saved = deliveryOrderRepository.save(order);
        return mapper.toDeliveryOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<DeliveryOrderResponse> getDeliveries(
            Long userId,
            DeliveryStatus status,
            DeliveryPriority priority,
            String query,
            Pageable pageable
    ) {
        String searchQuery = (query != null && !query.isBlank()) ? query.trim() : null;
        Page<DeliveryOrder> page = deliveryOrderRepository.searchDeliveries(userId, status, priority, searchQuery, pageable);
        return PagedResponse.of(page.map(mapper::toDeliveryOrderResponse));
    }

    @Transactional(readOnly = true)
    public DeliveryOrderResponse getDeliveryById(Long id, Long userId) {
        DeliveryOrder order = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery order not found with id: " + id));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this delivery order");
        }

        return mapper.toDeliveryOrderResponse(order);
    }

    @Transactional
    public DeliveryOrderResponse updateDelivery(Long id, Long userId, DeliveryOrderRequest request) {
        DeliveryOrder order = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery order not found with id: " + id));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this delivery order");
        }

        order.setCustomerName(request.customerName().trim());
        order.setAddress(request.address().trim());
        order.setLatitude(request.latitude());
        order.setLongitude(request.longitude());
        order.setContactPhone(request.contactPhone());
        order.setNotes(request.notes());
        if (request.priority() != null) order.setPriority(request.priority());
        if (request.status() != null) order.setStatus(request.status());

        DeliveryOrder updated = deliveryOrderRepository.save(order);
        return mapper.toDeliveryOrderResponse(updated);
    }

    @Transactional
    public DeliveryOrderResponse updateStatus(Long id, Long userId, DeliveryStatusUpdateRequest request) {
        DeliveryOrder order = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery order not found with id: " + id));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this delivery order");
        }

        order.setStatus(request.status());
        DeliveryOrder updated = deliveryOrderRepository.save(order);
        return mapper.toDeliveryOrderResponse(updated);
    }

    @Transactional
    public void deleteDelivery(Long id, Long userId) {
        DeliveryOrder order = deliveryOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery order not found with id: " + id));

        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You do not have access to this delivery order");
        }

        deliveryOrderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public List<DeliveryOrderResponse> getPendingDeliveries(Long userId) {
        return deliveryOrderRepository.findByUserIdAndStatus(userId, DeliveryStatus.PENDING).stream()
                .map(mapper::toDeliveryOrderResponse)
                .collect(Collectors.toList());
    }
}
