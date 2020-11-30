package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Corrections(
    @JsonProperty("approved") List<Long> applied,
    @JsonProperty("in_progress") List<Long> inProgress,
    @JsonProperty("rejected") List<Long> rejected
) { }