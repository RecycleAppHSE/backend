package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Tip(
    @JsonProperty("id") long id,
    @JsonProperty("collection_id") long collectionId,
    @JsonProperty("title") String title,
    @JsonProperty("content") String content) {}
