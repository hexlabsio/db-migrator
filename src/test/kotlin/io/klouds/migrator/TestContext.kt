package io.klouds.migrator

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger

class TestContext : Context {
    override fun getAwsRequestId() = "TestAwsRequestId"
    override fun getLogStreamName() = "TestLogStreamName"
    override fun getFunctionName() = "TestFunctionName"
    override fun getInvokedFunctionArn() = "TestInvokedFunctionArn"
    override fun getLogGroupName() = "TestLogGroupName"
    override fun getFunctionVersion() = "TestFunctionVersion"
    override fun getIdentity(): CognitoIdentity { TODO("not implemented") }
    override fun getClientContext(): ClientContext { TODO("not implemented") }
    override fun getLogger() = LambdaLogger { string -> println(string) }
    override fun getRemainingTimeInMillis() = -1
    override fun getMemoryLimitInMB() = 3008
}