package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

enum class RequestType { Create, Update, Delete }

enum class Status { SUCCESS, FAILED }

data class CustomResource(
        val lambdaLogger: LambdaLogger,
        val responseUrl: String,
        val logStreamName: String,
        val stackId: String,
        val requestId: String,
        val requestType: RequestType,
        val logicalResourceId: String
) {

    fun publish(status: Status, data: String) {
        (URL(responseUrl).openConnection() as HttpURLConnection).let {
            it.doOutput = true
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
                context.logger,
                input["ResponseURL"]!!.toString(),
                context.logStreamName,
                input["StackId"]?.toString() ?: "",
                input["RequestId"]?.toString() ?: "",
                RequestType.valueOf(input["RequestType"]!!.toString()),
                input["LogicalResourceId"]?.toString() ?: ""
        )
    }
}