package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class RootHandler : RequestHandler<Map<String, Any>, Any> {

    enum class RequestType { Create, Update, Delete }
    enum class Status { SUCCESS, FAILED }
    data class CustomResource(
        val context: Context,
        val responseUrl: String,
        val logStreamName: String,
        val stackId: String,
        val requestId: String,
        val requestType: RequestType,
        val logicalResourceId: String
    ) {

        fun publish(status: Status, data: String) {
            (URL(responseUrl).openConnection() as HttpURLConnection).let {
                it.doInput = true
                it.requestMethod = "PUT"
                val body = """{
                |   "Status": "${status.name}",
                |   "PhysicalResourceId": "$logStreamName",
                |   "StackId": "$stackId",
                |   "RequestId": "$requestId",
                |   "LogicalResourceId", "$logicalResourceId",
                |   "Data": $data
                |}""".trimMargin()
                OutputStreamWriter(it.outputStream).use { stream -> stream.write(body) }
            }
        }

        companion object {
            fun from(input: Map<String, Any>, context: Context) = CustomResource(
                    context,
                    input["ResponseURL"]!!.toString(),
                    context.logStreamName,
                    input["StackId"]?.toString() ?: "",
                    input["RequestId"]?.toString() ?: "",
                    RequestType.valueOf(input["RequestType"]!!.toString()),
                    input["LogicalResourceId"]?.toString() ?: ""
            )
        }
    }

    override fun handleRequest(input: Map<String, Any>, context: Context) {
        val customResource = CustomResource.from(input, context)
        customResource.publish(Status.SUCCESS, """{ "Message": "Success" }""")
    }
}