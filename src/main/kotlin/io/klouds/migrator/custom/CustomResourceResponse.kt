package io.klouds.migrator.custom

import com.fasterxml.jackson.annotation.JsonProperty

data class CustomResourceResponse<T>(
    @JsonProperty("Status") val status: Status,
    @JsonProperty("PhysicalResourceId") val physicalResourceId: String,
    @JsonProperty("StackId") val stackId: String,
    @JsonProperty("RequestId") val requestId: String,
    @JsonProperty("LogicalResourceId") val logicalResourceId: String,
    @JsonProperty("Data") val data: T,
    @JsonProperty("Reason") val reason: String?
)