package io.klouds.migrator.migration

import io.klouds.migrator.custom.Status

data class MigrationResponse(val success: Status, val migrations: Int, val errorMessage: String? = null)