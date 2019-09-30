package io.klouds.migrator.custom

import com.fasterxml.jackson.annotation.JsonProperty

data class ResourceProperties(
    @JsonProperty("DatabaseUrl") val databaseUrl: String,
    @JsonProperty("MigrationBucket") val migrationBucket: String,
    @JsonProperty("MigrationKey") val migrationKey: String
)

data class CustomResourceRequest(
    @JsonProperty("ResourceProperties") val resourceProperties: ResourceProperties,
    @JsonProperty("OldResourceProperties") val oldResourceProperties: ResourceProperties?,
    @JsonProperty("ResponseURL") val responseUrl: String,
    @JsonProperty("StackId") val stackId: String,
    @JsonProperty("RequestId") val requestId: String,
    @JsonProperty("RequestType") val requestType: RequestType,
    @JsonProperty("LogicalResourceId") val logicalResourceId: String
)