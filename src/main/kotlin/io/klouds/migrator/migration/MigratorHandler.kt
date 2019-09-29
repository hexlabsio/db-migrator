package io.klouds.migrator.migration

import com.fasterxml.jackson.module.kotlin.readValue
import io.klouds.migrator.Context
import io.klouds.migrator.Handler

// class MigratorHandler2 : RequestHandler<MigrationRequest, Any> {
// 
//     override fun handleRequest(input: MigrationRequest, context: Context) {
//         context.logger.log("Migration Started")
//         println(input)
//         context.logger.log("Downloading")
// //         updateMigrations(AmazonS3Client.builder()
// //                 .withRegion(Regions.EU_WEST_1)
// //                 .enablePathStyleAccess()
// //                 .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration("com.amazonaws.eu-west-1.s3", "eu-west-1"))
// //                 .build().getObject(input["Bucket"]!!.toString(), input["Key"]!!.toString()).objectContent)
// //         migrate(input["DatabaseURL"]!!.toString(), "master", "masterSecret")
//         context.logger.log("Migration Ended")
//     }
// 
//     fun updateMigrations(inputStream: InputStream) {
//         println("Updating tmp")
//         val zipInputStream = ZipInputStream(inputStream)
//         val destination = File(migrationLocation)
//         if (destination.exists()) {
//             destination.deleteRecursively()
//         }
//         destination.mkdirs()
//         var zipEntry = zipInputStream.nextEntry
//         while (zipEntry != null) {
//             println("ahahahah")
//             val newFile = File(destination, zipEntry.name)
//             FileOutputStream(newFile).use {
//                 it.write(zipInputStream.readBytes())
//             }
//             zipEntry = zipInputStream.nextEntry
//         }
//         zipInputStream.closeEntry()
//         zipInputStream.close()
//     }
// 
//     fun migrate(databaseUrl: String, username: String, password: String) {
//         println("Connecting to $databaseUrl")
//         File("/tmp/db/migration").mkdirs()
//         val flyway = Flyway.configure().locations(Location("$FILESYSTEM_PREFIX$migrationLocation"))
//                 .dataSource("jdbc:postgresql://$databaseUrl", username, password).load()
//         flyway.migrate()
//         println("Done")
//     }
// 
//     companion object {
//         val migrationLocation = "/tmp/db/migration"
// //         val migrationHandler = lambdaHandler { request: MigrationRequest ->
// //             println(request.toString())
// //             MigrationResponse(true)
// //         }
//     }
// }

class MigratorHandler : Handler<MigrationRequest, MigrationResponse>(objectMapper::readValue) {
    override fun Context.handle(request: MigrationRequest): MigrationResponse {
        println(request.toString())
        return MigrationResponse(true)
    }
}