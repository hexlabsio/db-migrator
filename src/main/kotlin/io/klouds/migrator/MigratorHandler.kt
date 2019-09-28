package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.s3.AmazonS3Client
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.api.Location.FILESYSTEM_PREFIX
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipInputStream
import java.io.FileOutputStream
import java.io.InputStream

class MigratorHandler : RequestHandler<Map<String, Any>, Any> {

    override fun handleRequest(input: Map<String, Any>, context: Context) {
        context.logger.log("Migration Started")
        input.map { context.logger.log("${it.key}: ${it.value}") }
        updateMigrations(AmazonS3Client.builder().build().getObject(input["Bucket"]!!.toString(), input["Key"]!!.toString()).objectContent)
        migrate(input["DatabaseURL"]!!.toString(), "master", "masterSecret")
        context.logger.log("Migration Ended")
    }

    fun updateMigrations(inputStream: InputStream) {
        val zipInputStream = ZipInputStream(inputStream)
        val destination = File(migrationLocation)
        if (destination.exists()) {
            destination.deleteRecursively()
        }
        destination.mkdirs()
        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            val newFile = File(destination, zipEntry.name)
            FileOutputStream(newFile).use {
                it.write(zipInputStream.readBytes())
            }
            zipEntry = zipInputStream.nextEntry
        }
        zipInputStream.closeEntry()
        zipInputStream.close()
    }

    fun migrate(databaseUrl: String, username: String, password: String) {
        println("Connecting to $databaseUrl")
        File("/tmp/db/migration").mkdirs()
        val flyway = Flyway.configure().locations(Location("$FILESYSTEM_PREFIX$migrationLocation"))
                .dataSource("jdbc:postgresql://$databaseUrl", username, password).load()
        flyway.migrate()
        println("Done")
    }

    companion object {
        val migrationLocation = "/tmp/db/migration"
    }
}

fun main() {
    MigratorHandler().let {
        it.updateMigrations(FileInputStream("/Users/chrisbarbour/Code/db-migrator/src/main/resources/db/migration/migrations.zip"))
        it.migrate("localhost:5432/postgres", "postgres", "postgres")
    }
}