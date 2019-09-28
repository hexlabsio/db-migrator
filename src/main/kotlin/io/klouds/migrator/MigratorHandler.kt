package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

class MigratorHandler : RequestHandler<Map<String, Any>, Any> {

    override fun handleRequest(input: Map<String, Any>, context: Context) {
        context.logger.log("Migration Started")
        input.map { context.logger.log("${it.key}: ${it.value}") }
        context.logger.log("Migration Ended")
    }
}