package io.klouds.migrator.custom

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

data class CustomResourceResponse<T>(
    @JsonProperty("Status") val status: Status,
    @JsonProperty("PhysicalResourceId") val physicalResourceId: String,
    @JsonProperty("StackId") val stackId: String,
    @JsonProperty("RequestId") val requestId: String,
    @JsonProperty("LogicalResourceId") val logicalResourceId: String,
    @JsonProperty("Data") val data: T,
    @JsonInclude(JsonInclude.Include.NON_NULL) @JsonProperty("Reason") val reason: String?
)