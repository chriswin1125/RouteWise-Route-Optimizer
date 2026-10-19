package com.routewise.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "default_depot_address")
    private String defaultDepotAddress;

    @Column(name = "default_depot_lat")
    private Double defaultDepotLat;

    @Column(name = "default_depot_lng")
    private Double defaultDepotLng;

    @Column(name = "distance_unit", length = 20)
    private String distanceUnit = "km";

    @Column(name = "default_speed_kmh")
    private Double defaultSpeedKmh = 40.0;

    @Column(name = "avoid_tolls")
    private Boolean avoidTolls = false;

    @Column(length = 20)
    private String theme = "light";

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public UserPreference() {}

    public UserPreference(User user) {
        this.user = user;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
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

    public String getDefaultDepotAddress() {
        return defaultDepotAddress;
    }

    public void setDefaultDepotAddress(String defaultDepotAddress) {
        this.defaultDepotAddress = defaultDepotAddress;
    }

    public Double getDefaultDepotLat() {
        return defaultDepotLat;
    }

    public void setDefaultDepotLat(Double defaultDepotLat) {
        this.defaultDepotLat = defaultDepotLat;
    }

    public Double getDefaultDepotLng() {
        return defaultDepotLng;
    }

    public void setDefaultDepotLng(Double defaultDepotLng) {
        this.defaultDepotLng = defaultDepotLng;
    }

    public String getDistanceUnit() {
        return distanceUnit;
    }

    public void setDistanceUnit(String distanceUnit) {
        this.distanceUnit = distanceUnit;
    }

    public Double getDefaultSpeedKmh() {
        return defaultSpeedKmh;
    }

    public void setDefaultSpeedKmh(Double defaultSpeedKmh) {
        this.defaultSpeedKmh = defaultSpeedKmh;
    }

    public Boolean getAvoidTolls() {
        return avoidTolls;
    }

    public void setAvoidTolls(Boolean avoidTolls) {
        this.avoidTolls = avoidTolls;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
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
}
