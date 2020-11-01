package io.rcapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record User(
    @JsonProperty("id") Long id,
    @JsonProperty("name") String name,
    @JsonProperty("photo_url") String photoUrl,
    @JsonProperty("favourite_news_ids") List<Long> favouriteNewsIds,
    @JsonProperty("collection_points_corrections_ids") Corrections collectionPointsCorrectionsIds){

}
