package io.klouds.migrator.migration

data class MigrationResponse(val success: Boolean, val migrations: Int, val errorMessage: String?)