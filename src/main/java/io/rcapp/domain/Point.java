package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Point(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("phone_number") String phoneNumber,
    @JsonProperty("web_site") String webSite,
    @JsonProperty("recycle") List<String> recycle,
    @JsonProperty("latitude") float latitude,
    @JsonProperty("longitude") float longitude,
    @JsonProperty("works") String works,
    @JsonProperty("last_updated") Long lastUpdated,
    @JsonProperty("schedule") Schedule schedule,
    @JsonProperty("corrections_count") Long correctionsCount
) { }
