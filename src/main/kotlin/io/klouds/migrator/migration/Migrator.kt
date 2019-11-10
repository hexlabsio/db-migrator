package io.klouds.migrator.migration

import io.klouds.migrator.custom.Status
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.Location.FILESYSTEM_PREFIX
import java.lang.Exception

interface Migrator<Result> {
    fun migrate(dataSourceUrl: String, username: String, password: String, schemas: List<String>, clean: Boolean): Result
}

class FlywayMigrator(
    private val jdbcUrlFor: (String) -> String = { "jdbc:postgresql://$it" },
    private val migrationsDirectory: String = MIGRATIONS_DIR
) : Migrator<MigrationResponse> {

    private fun flyway(dataSourceUrl: String, username: String, password: String, schemas: List<String>) =
        Flyway.configure()
        .schemas(*schemas.toTypedArray())
        .locations(Location("$FILESYSTEM_PREFIX$migrationsDirectory"))
        .dataSource(jdbcUrlFor(dataSourceUrl), username, password)
        .load()

    override fun migrate(dataSourceUrl: String, username: String, password: String, schemas: List<String>, clean: Boolean) =
        flyway(dataSourceUrl, username, password, schemas).run {
            if (clean) clean()
            startMigration()
                    .let { (migrations, exception) ->
                        if (exception != null) {
                            MigrationResponse(Status.FAILED, migrations, exception.message)
                        } else {
                            MigrationResponse(Status.SUCCESS, migrations, "Invoked $migrations Migration(s)")
                        }
                    }
        }

    companion object {
        const val MIGRATIONS_DIR = "/tmp/db/migration"
    }
}

private fun Flyway.startMigration(): Pair<Int, Exception?> {
    return try { migrate() to null } catch (e: Exception) { 0 to e }
}

fun main() {
    FlywayMigrator(
            migrationsDirectory = "/Users/chrisbarbour/Code/klouds-inventory/.circleci/db-migrations"
    ).migrate("localhost:5432/postgres", "postgres", "postgres", listOf("jimmy", "brian"), true).information?.let { println(it) }
}