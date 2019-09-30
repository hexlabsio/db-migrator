package io.klouds.migrator.migration

import io.klouds.migrator.custom.Status
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.Location.FILESYSTEM_PREFIX

interface Migrator<Result> {
    fun migrate(dataSourceUrl: String, username: String, password: String): Result
    fun delete(dataSourceUrl: String, username: String, password: String): Result
}

class FlywayMigrator(
    private val jdbcUrlFor: (String) -> String = { "jdbc:postgresql://$it" },
    private val migrationsDirectory: String = MIGRATIONS_DIR
) : Migrator<MigrationResponse> {

    private fun flyway(dataSourceUrl: String, username: String, password: String) =
        Flyway.configure()
        .locations(Location("$FILESYSTEM_PREFIX$migrationsDirectory"))
        .dataSource(jdbcUrlFor(dataSourceUrl), username, password)
        .load()

    override fun migrate(dataSourceUrl: String, username: String, password: String) =
        flyway(dataSourceUrl, username, password).startMigration()
                .let { (migrations, exception) ->
                    if (exception != null) {
                        MigrationResponse(Status.FAILED, migrations, exception.message)
                    } else {
                        MigrationResponse(Status.SUCCESS, migrations, "Invoked $migrations Migration(s)")
                    }
                }

    override fun delete(dataSourceUrl: String, username: String, password: String): MigrationResponse {
        flyway(dataSourceUrl, username, password).clean()
        return MigrationResponse(Status.SUCCESS, 0)
    }

    companion object {
        const val MIGRATIONS_DIR = "/tmp/db/migration"
    }
}

private fun Flyway.startMigration(): Pair<Int, FlywayException?> {
    return try { migrate() to null } catch (e: FlywayException) { 0 to e }
}