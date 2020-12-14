package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TipCollection(
    @JsonProperty("id") long id,
    @JsonProperty("title") String title,
    @JsonProperty("tips_number") int tipsNumber) {}
