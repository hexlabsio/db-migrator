package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import java.io.BufferedReader
import java.io.InputStreamReader
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
        lambdaLogger.log("Publishing status $status and data $data to $responseUrl")
        val body = """{
                |   "Status": "${status.name}",
                |   "PhysicalResourceId": "$logStreamName",
                |   "StackId": "$stackId",
                |   "RequestId": "$requestId",
                |   "LogicalResourceId": "$logicalResourceId",
                |   "Data": $data
                |}""".trimMargin()
        lambdaLogger.log("Sending")
        URL(responseUrl).put(body)
        lambdaLogger.log("Finished")
    }

    private fun URL.put(body: String) = with(openConnection() as HttpURLConnection) {
        requestMethod = "PUT"
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        OutputStreamWriter(outputStream).let {
            it.write(body)
            it.flush()
        }
        lambdaLogger.log("URL : $url")
        lambdaLogger.log("BODY : $body")
        lambdaLogger.log("Response Code : $responseCode")
        BufferedReader(InputStreamReader(inputStream)).readText()
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