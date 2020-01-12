package io.klouds.migrator.migration

data class MigrationRequest(
    val bucket: String? = null,
    val key: String? = null,
    val databaseUrl: String? = null,
    val username: String? = null,
    val secretLocation: String? = null,
    val clean: Boolean = false,
    val schemas: List<String> = emptyList()
)
