package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

val objectMapper = jacksonObjectMapper()

inline fun <reified Request, reified Response> lambdaHandler(crossinline handler: Context.(Request) -> Response) =
    RequestStreamHandler { input, output, context ->
        output.write(objectMapper.writeValueAsBytes(with(context) { handler(objectMapper.readValue(input)) }))
    }