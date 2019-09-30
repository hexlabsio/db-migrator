package io.klouds.migrator.custom

import io.klouds.migrator.Handler.Companion.objectMapper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

interface EventPublisher<T, R> {
    fun publish(event: T, to: String): R
}

class StackEventPublisher : EventPublisher<CustomResourceResponse<ResponseMessage>, String> {
    override fun publish(event: CustomResourceResponse<ResponseMessage>, to: String) =
        URL(to).put(objectMapper.writeValueAsString(event))

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
}