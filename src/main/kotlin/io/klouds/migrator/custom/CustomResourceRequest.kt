package io.klouds.migrator.custom

import com.fasterxml.jackson.annotation.JsonProperty

data class ResourceProperties(
    @JsonProperty("DatabaseUrl") val databaseUrl: String? = null,
    @JsonProperty("DatabaseUser") val databaseUser: String? = null,
    @JsonProperty("DatabaseSecretLocation") val databaseSecretLocation: String? = null,
    @JsonProperty("MigrationBucket") val migrationBucket: String? = null,
    @JsonProperty("MigrationKey") val migrationKey: String? = null,
    @JsonProperty("Clean") val clean: Boolean = false,
    @JsonProperty("Schemas") val schemas: List<String> = emptyList()
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