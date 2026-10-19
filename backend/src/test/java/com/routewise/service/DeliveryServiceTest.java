package com.routewise.service;

import com.routewise.dto.delivery.DeliveryOrderRequest;
import com.routewise.dto.delivery.DeliveryOrderResponse;
import com.routewise.dto.delivery.DeliveryStatusUpdateRequest;
import com.routewise.entity.DeliveryOrder;
import com.routewise.entity.DeliveryPriority;
import com.routewise.entity.DeliveryStatus;
import com.routewise.entity.User;
import com.routewise.exception.UnauthorizedAccessException;
import com.routewise.mapper.EntityDtoMapper;
import com.routewise.repository.DeliveryOrderRepository;
import com.routewise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    @Mock
    private UserRepository userRepository;

    private EntityDtoMapper mapper;
    private DeliveryService deliveryService;

    private User user1;
    private User user2;
    private DeliveryOrder order1;

    @BeforeEach
    void setUp() {
        mapper = new EntityDtoMapper();
        deliveryService = new DeliveryService(deliveryOrderRepository, userRepository, mapper);

        user1 = new User("user1@test.com", "hash", "User One", "Company 1");
        user1.setId(1L);

        user2 = new User("user2@test.com", "hash", "User Two", "Company 2");
        user2.setId(2L);

        order1 = new DeliveryOrder();
        order1.setId(10L);
        order1.setUser(user1);
        order1.setCustomerName("Acme Corp");
        order1.setAddress("123 Main St, San Francisco, CA");
        order1.setLatitude(37.7749);
        order1.setLongitude(-122.4194);
        order1.setStatus(DeliveryStatus.PENDING);
        order1.setPriority(DeliveryPriority.HIGH);
    }

    @Test
    @DisplayName("Should successfully create a delivery order for user")
    void testCreateDelivery() {
        DeliveryOrderRequest request = new DeliveryOrderRequest(
                "Acme Corp", "123 Main St, San Francisco, CA", 37.7749, -122.4194,
                "+1-555-0100", "Handle with care", DeliveryPriority.HIGH, DeliveryStatus.PENDING
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenAnswer(invocation -> {
            DeliveryOrder d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });

        DeliveryOrderResponse response = deliveryService.createDelivery(1L, request);

        assertNotNull(response);
        assertEquals("Acme Corp", response.customerName());
        assertEquals(DeliveryPriority.HIGH, response.priority());
        assertEquals(DeliveryStatus.PENDING, response.status());
    }

    @Test
    @DisplayName("Should prevent user2 from accessing user1's delivery order (data isolation)")
    void testUnauthorizedDeliveryAccess() {
        when(deliveryOrderRepository.findById(10L)).thenReturn(Optional.of(order1));

        assertThrows(UnauthorizedAccessException.class, () -> deliveryService.getDeliveryById(10L, 2L));
        assertThrows(UnauthorizedAccessException.class, () -> deliveryService.deleteDelivery(10L, 2L));
    }

    @Test
    @DisplayName("Should update delivery order status successfully")
    void testUpdateStatus() {
        when(deliveryOrderRepository.findById(10L)).thenReturn(Optional.of(order1));
        when(deliveryOrderRepository.save(any(DeliveryOrder.class))).thenReturn(order1);

        DeliveryStatusUpdateRequest updateReq = new DeliveryStatusUpdateRequest(DeliveryStatus.DELIVERED);
        DeliveryOrderResponse response = deliveryService.updateStatus(10L, 1L, updateReq);

        assertEquals(DeliveryStatus.DELIVERED, response.status());
    }
}
