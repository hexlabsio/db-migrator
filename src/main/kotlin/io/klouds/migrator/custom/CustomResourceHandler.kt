package io.klouds.migrator.custom

import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.klouds.migrator.CallbackHandler
import io.klouds.migrator.Handler.Companion.objectMapper
import io.klouds.migrator.defaultTransform
import io.klouds.migrator.migration.MigrationRequest
import io.klouds.migrator.migration.MigrationResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

data class ResponseMessage(
    @JsonProperty("Migrations") val migrations: Int
)

class CustomResourceHandler : CallbackHandler<CustomResourceRequest>(defaultTransform()) {
    override fun Context.handle(request: CustomResourceRequest) {
        fun customResourceResponse(status: Status, migrations: Int, errorMessage: String?) = CustomResourceResponse(
                status, logStreamName, request.stackId, request.requestId, request.logicalResourceId,
                ResponseMessage(migrations), errorMessage
        )
        try {
            val migrator = System.getenv("MIGRATOR_ARN")
            val name = migrator.substringAfter("function:")
            logger.log("Invoking Migrator at $migrator")
            val properties = request.resourceProperties
            val migrationRequest = MigrationRequest(properties.migrationBucket, properties.migrationKey, properties.databaseUrl, "master")
            val migrationResponse = AWSLambdaClient.builder().build().invoke(
                    InvokeRequest()
                            .withFunctionName(name)
                            .withPayload(objectMapper.writeValueAsString(migrationRequest))
            )
            val output = objectMapper.readValue<MigrationResponse>(String(migrationResponse.payload.array()))
            URL(request.responseUrl).put(objectMapper.writeValueAsString(customResourceResponse(
                    if (output.success) Status.SUCCESS else Status.FAILED,
                    output.migrations,
                    output.errorMessage
            )))
        } catch (e: Exception) {
            logger.log(e.message)
            URL(request.responseUrl).put(objectMapper.writeValueAsString(customResourceResponse(
                    Status.FAILED, 0, e.message
            )))
        }
    }
}

private fun URL.put(body: String) = with(openConnection() as HttpURLConnection) {
    requestMethod = "PUT"
    doOutput = true
    setRequestProperty("Content-Type", "application/json")
    OutputStreamWriter(outputStream).let {
        it.write(body)
        it.flush()
    }
    BufferedReader(InputStreamReader(inputStream)).readText()
}
