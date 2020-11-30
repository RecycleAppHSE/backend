package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record User(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("photo_url") String photoUrl,
    @JsonProperty("collection_points_corrections_ids") Corrections collectionPointsCorrectionsIds
) { }
