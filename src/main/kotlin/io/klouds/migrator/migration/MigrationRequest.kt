package io.klouds.migrator.migration

data class MigrationRequest(
    val bucket: String?,
    val key: String?,
    val databaseUrl: String?,
    val username: String?,
    val secretLocation: String?,
    val clean: Boolean = false,
    val schemas: List<String> = emptyList()
)
