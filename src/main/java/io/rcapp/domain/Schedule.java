package io.rcapp.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Schedule(@JsonProperty("from") String from, @JsonProperty("to") String to) {}
