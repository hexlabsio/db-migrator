package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.lang.Exception

class RootHandler : RequestHandler<Map<String, Any>, Any> {

    override fun handleRequest(input: Map<String, Any>, context: Context) {
        val logger = context.logger
        input.forEach { (key, value) -> logger.log("Found: $key = $value") }
        val customResource = CustomResource.from(input, context)
        try {
            customResource.publish(Status.SUCCESS, """{ "Message": "Success" }""")
        } catch (e: Exception) {
            customResource.publish(Status.FAILED, """{ "Message": "Failed" }""")
        }
    }
}