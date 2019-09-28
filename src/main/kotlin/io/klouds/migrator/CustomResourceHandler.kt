package io.klouds.migrator

import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.lang.Exception

class CustomResourceHandler : RequestHandler<Map<String, Any>, Any> {

    private val databaseUrlKey = "DatabaseURL"

    data class ResourceProperties(val current: Map<String, String>, val old: Map<String, String>) {
        operator fun get(key: String) = current[key]
        companion object {
            fun from(input: Map<String, Any>) = ResourceProperties(
                    current = input["ResourceProperties"]!! as Map<String, String>,
                    old = input["ResourceProperties"] as? Map<String, String> ?: emptyMap()
            )
        }
    }

    override fun handleRequest(input: Map<String, Any>, context: Context) {
        val logger = context.logger
        input.forEach { (key, value) -> logger.log("Found: $key = $value") }
        val resourceProperties = ResourceProperties.from(input)
        val databaseUrl = resourceProperties[databaseUrlKey]
        logger.log("DatabaseURL = $databaseUrl")
        val customResource = CustomResource.from(input, context)
        try {
            val migrator = System.getenv("MIGRATOR_ARN")
            val name = migrator.substringAfter("function:")
            logger.log("Invoking Migrator at $migrator")
            val response = AWSLambdaClient.builder().build().invoke(
                    InvokeRequest()
                            .withFunctionName(name)
                            .withPayload("""{ "DatabaseURL": "$databaseUrl" }""")
            )
            val output = String(response.payload.array())
            logger.log("Received $output")
            customResource.publish(Status.SUCCESS, """{ "Message": "Success" }""")
        } catch (e: Exception) {
            logger.log(e.message)
            customResource.publish(Status.FAILED, """{ "Message": "Failed" }""")
        }
        logger.log("End")
    }
}