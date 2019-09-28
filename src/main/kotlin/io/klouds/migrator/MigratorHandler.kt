package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.flywaydb.core.Flyway

class MigratorHandler : RequestHandler<Map<String, Any>, Any> {

    override fun handleRequest(input: Map<String, Any>, context: Context) {
        context.logger.log("Migration Started")
        input.map { context.logger.log("${it.key}: ${it.value}") }
        migrate(input["DatabaseURL"]!!.toString())
        context.logger.log("Migration Ended")
    }
}

fun migrate(databaseUrl: String) {
    println("Connecting to $databaseUrl")
    val flyway = Flyway.configure().dataSource("jdbc:postgresql://$databaseUrl/kloudsInventory", "master", "masterSecret").load()
    flyway.migrate()
    println("Done")
}