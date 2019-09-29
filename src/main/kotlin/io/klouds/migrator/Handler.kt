package io.klouds.migrator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream
import java.io.OutputStream

interface StreamHandler {
    fun handleRequest(input: InputStream, output: OutputStream, context: Context)
}

abstract class Handler<Request, Response>(
    val requestTransform: (InputStream) -> Request
) : StreamHandler {
    override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
        output.write(objectMapper.writeValueAsBytes(with(context) { handle(requestTransform(input)) }))
    }
    abstract fun Context.handle(request: Request): Response
    companion object {
        val objectMapper = jacksonObjectMapper()
    }
}