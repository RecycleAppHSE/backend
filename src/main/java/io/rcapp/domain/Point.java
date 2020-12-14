package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;

public record Point(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("address") String address,
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("web_site") String webSite,
    @JsonProperty("recycle") Set<String> recycle,
    @JsonProperty("latitude") double latitude,
    @JsonProperty("longitude") double longitude,
    @JsonProperty("works") String works,
    @JsonProperty("last_updated") Long lastUpdated,
    @JsonProperty("schedule") Schedule schedule,
    @JsonProperty("corrections_count") Long correctionsCount) {}
