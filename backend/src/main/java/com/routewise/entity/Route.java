package com.routewise.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "depot_address", nullable = false, length = 255)
    private String depotAddress;

    @Column(name = "depot_lat", nullable = false)
    private Double depotLatitude;

    @Column(name = "depot_lng", nullable = false)
    private Double depotLongitude;

    @Column(name = "end_depot_address", length = 255)
    private String endDepotAddress;

    @Column(name = "end_depot_lat")
    private Double endDepotLatitude;

    @Column(name = "end_depot_lng")
    private Double endDepotLongitude;

    @Column(name = "round_trip", nullable = false)
    private Boolean roundTrip = true;

    @Column(name = "optimization_method", nullable = false, length = 50)
    private String optimizationMethod;

    @Column(name = "total_distance_km", nullable = false)
    private Double totalDistanceKm;

    @Column(name = "estimated_duration_min", nullable = false)
    private Double estimatedDurationMin;

    @Column(name = "route_geometry", columnDefinition = "TEXT")
    private String routeGeometry; // GeoJSON coordinates string

    @Column(name = "is_estimated", nullable = false)
    private Boolean isEstimated = false;

    @Column(name = "baseline_distance_km")
    private Double baselineDistanceKm;

    @Column(name = "baseline_duration_min")
    private Double baselineDurationMin;

    @Column(name = "distance_saved_km")
    private Double distanceSavedKm = 0.0;

    @Column(name = "savings_percentage")
    private Double savingsPercentage = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopSequence ASC")
    private List<RouteStop> stops = new ArrayList<>();

    public Route() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void addStop(RouteStop stop) {
        stops.add(stop);
        stop.setRoute(this);
    }

    public void removeStop(RouteStop stop) {
        stops.remove(stop);
        stop.setRoute(null);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepotAddress() {
        return depotAddress;
    }

    public void setDepotAddress(String depotAddress) {
        this.depotAddress = depotAddress;
    }

    public Double getDepotLatitude() {
        return depotLatitude;
    }

    public void setDepotLatitude(Double depotLatitude) {
        this.depotLatitude = depotLatitude;
    }

    public Double getDepotLongitude() {
        return depotLongitude;
    }

    public void setDepotLongitude(Double depotLongitude) {
        this.depotLongitude = depotLongitude;
    }

    public String getEndDepotAddress() {
        return endDepotAddress;
    }

    public void setEndDepotAddress(String endDepotAddress) {
        this.endDepotAddress = endDepotAddress;
    }

    public Double getEndDepotLatitude() {
        return endDepotLatitude;
    }

    public void setEndDepotLatitude(Double endDepotLatitude) {
        this.endDepotLatitude = endDepotLatitude;
    }

    public Double getEndDepotLongitude() {
        return endDepotLongitude;
    }

    public void setEndDepotLongitude(Double endDepotLongitude) {
        this.endDepotLongitude = endDepotLongitude;
    }

    public Boolean getRoundTrip() {
        return roundTrip;
    }

    public void setRoundTrip(Boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public String getOptimizationMethod() {
        return optimizationMethod;
    }

    public void setOptimizationMethod(String optimizationMethod) {
        this.optimizationMethod = optimizationMethod;
    }

    public Double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public Double getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(Double estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public String getRouteGeometry() {
        return routeGeometry;
    }

    public void setRouteGeometry(String routeGeometry) {
        this.routeGeometry = routeGeometry;
    }

    public Boolean getIsEstimated() {
        return isEstimated;
    }

    public void setIsEstimated(Boolean isEstimated) {
        this.isEstimated = isEstimated;
    }

    public Double getBaselineDistanceKm() {
        return baselineDistanceKm;
    }

    public void setBaselineDistanceKm(Double baselineDistanceKm) {
        this.baselineDistanceKm = baselineDistanceKm;
    }

    public Double getBaselineDurationMin() {
        return baselineDurationMin;
    }

    public void setBaselineDurationMin(Double baselineDurationMin) {
        this.baselineDurationMin = baselineDurationMin;
    }

    public Double getDistanceSavedKm() {
        return distanceSavedKm;
    }

    public void setDistanceSavedKm(Double distanceSavedKm) {
        this.distanceSavedKm = distanceSavedKm;
    }

    public Double getSavingsPercentage() {
        return savingsPercentage;
    }

    public void setSavingsPercentage(Double savingsPercentage) {
        this.savingsPercentage = savingsPercentage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<RouteStop> getStops() {
        return stops;
    }

    public void setStops(List<RouteStop> stops) {
        this.stops = stops;
    }
}
