package io.klouds.migrator.migration

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.s3.AmazonS3Client
import com.fasterxml.jackson.module.kotlin.readValue
import io.klouds.migrator.Handler
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.Location.FILESYSTEM_PREFIX

class MigratorHandler(
    private val downloader: Downloader = S3ZipDownloader(vpcS3Client()),
    private val secretFinder: SecretFinder = object : SecretFinder {
        override fun secretFor(key: String) = when (key) {
            "master" -> "masterSecret"
            else -> "postgres"
        }
    }
) : Handler<MigrationRequest, MigrationResponse>(objectMapper::readValue) {
    override fun Context.handle(request: MigrationRequest): MigrationResponse {
        logger.log("Downloading ${request.bucket}/${request.key} into $MIGRATION_DIR")
        downloader.download(request.bucket, request.key, MIGRATION_DIR)
        logger.log("Migrating database ${request.databaseUrl}")
        return Flyway.configure()
            .locations(Location("$FILESYSTEM_PREFIX$MIGRATION_DIR"))
            .dataSource("jdbc:postgresql://${request.databaseUrl}", request.username, secretFinder.secretFor(request.username))
            .load().startMigration().let { (migrations, exception) ->
                MigrationResponse(exception == null, migrations, exception?.message)
            }
    }

    private fun Flyway.startMigration(): Pair<Int, FlywayException?> {
        return try { migrate() to null } catch (e: FlywayException) { 0 to e }
    }

    companion object {
        private const val REGION = "eu-west-1"
        private const val MIGRATION_DIR = "/tmp/db/migration"
        private fun vpcS3Client() = AmazonS3Client.builder()
            .withRegion(REGION)
            .enablePathStyleAccess()
            .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("com.amazonaws.eu-west-1.s3", REGION))
            .build()
    }
}
