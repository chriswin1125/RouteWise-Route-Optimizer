package com.routewise.dto.location;

public record LocationSearchDto(
        String placeId,
        String displayName,
        Double latitude,
        Double longitude,
        String type,
        String city,
        String state,
        String country
) {}
