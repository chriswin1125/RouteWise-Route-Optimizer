package com.routewise.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "route_stops")
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_order_id")
    private DeliveryOrder deliveryOrder;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "customer_name", length = 150)
    private String customerName;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "estimated_arrival")
    private OffsetDateTime estimatedArrival;

    @Column(name = "distance_to_next_km")
    private Double distanceToNextKm;

    @Column(name = "duration_to_next_min")
    private Double durationToNextMin;

    public RouteStop() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public Integer getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(Integer stopSequence) {
        this.stopSequence = stopSequence;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public OffsetDateTime getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(OffsetDateTime estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public Double getDistanceToNextKm() {
        return distanceToNextKm;
    }

    public void setDistanceToNextKm(Double distanceToNextKm) {
        this.distanceToNextKm = distanceToNextKm;
    }

    public Double getDurationToNextMin() {
        return durationToNextMin;
    }

    public void setDurationToNextMin(Double durationToNextMin) {
        this.durationToNextMin = durationToNextMin;
    }
}
