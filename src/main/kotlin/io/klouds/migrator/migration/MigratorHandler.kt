package io.klouds.migrator.migration

import com.amazonaws.services.lambda.runtime.Context
import io.klouds.migrator.Handler
import io.klouds.migrator.defaultTransform
import io.klouds.migrator.migration.FlywayMigrator.Companion.MIGRATIONS_DIR

class MigratorHandler(
    private val downloader: Downloader = S3ZipDownloader(),
    private val secretFinder: SecretFinder = AwsSecretFinder(),
    private val migrator: Migrator<MigrationResponse> = FlywayMigrator()
) : Handler<MigrationRequest, MigrationResponse>(defaultTransform()) {
    override fun Context.handle(request: MigrationRequest): MigrationResponse {
        logger.log("Downloading ${request.bucket}/${request.key} into $MIGRATIONS_DIR")
        downloader.download(request.bucket, request.key, MIGRATIONS_DIR)
        logger.log("Migrating database ${request.databaseUrl}")
        return migrator.migrate(request.databaseUrl, request.username, secretFinder.secretFor(request.secretLocation))
    }
}
