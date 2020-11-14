package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PointIdWIthLocation(
    @JsonProperty("id") Long id,
    @JsonProperty("latitude") double latitude,
    @JsonProperty("longitude") double longitude
) { }