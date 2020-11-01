package io.rcapp;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record Corrections(
    @JsonProperty("approved") List<Long> approved,
    @JsonProperty("not_approved") List<Long> notApproved){

}