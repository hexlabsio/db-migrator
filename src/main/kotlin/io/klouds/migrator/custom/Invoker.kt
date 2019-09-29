package io.klouds.migrator.custom

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import com.fasterxml.jackson.module.kotlin.readValue
import io.klouds.migrator.Handler.Companion.objectMapper
import io.klouds.migrator.migration.MigrationRequest
import io.klouds.migrator.migration.MigrationResponse

interface Invoker <T, R> {
    fun invoke(function: String, request: T): R
}

class MigrationLambdaInvoker(
    val lambdaClient: AWSLambda = AWSLambdaClient.builder().build()
) : Invoker<MigrationRequest, MigrationResponse> {
    override fun invoke(function: String, request: MigrationRequest) = objectMapper.readValue<MigrationResponse>(
            String(
                    lambdaClient.invoke(
                            InvokeRequest()
                                .withFunctionName(function)
                                .withPayload(objectMapper.writeValueAsString(request))
                    ).payload.array()
            )
    )
}