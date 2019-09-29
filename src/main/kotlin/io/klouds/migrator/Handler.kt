package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import java.io.OutputStream

abstract class Handler<Request, Response>(
    val requestTransform: (InputStream) -> Request
) : RequestStreamHandler {
    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        output.write(objectMapper.writeValueAsBytes(with(context) { handle(requestTransform(input)) }))
    }
    abstract fun Context.handle(request: Request): Response
    companion object {
        val objectMapper = jacksonObjectMapper()
    }
}

abstract class CallbackHandler<Request>(
    val requestTransform: (InputStream) -> Request
) : RequestHandler<InputStream, String> {
    override fun handleRequest(input: InputStream, context: Context): String {
        with(context) { handle(requestTransform(input)) }
        return "Done"
    }
    abstract fun Context.handle(request: Request)
}

inline fun <reified T> defaultTransform(): (InputStream) -> T = Handler.objectMapper::readValue