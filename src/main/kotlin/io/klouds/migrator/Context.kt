package io.klouds.migrator

interface Logger {
    fun log(text: String)
}

interface Context {
    fun getAwsRequestId() = ""
    fun getLogGroupName() = ""
    fun getLogStreamName() = ""
    fun getFunctionName() = ""
    fun getFunctionVersion() = ""
    fun getInvokedFunctionArn() = ""
    fun getIdentity(): Any? = null // CognitoIdentity
    fun getClientContext(): Any? = null // ClientContext
    fun getRemainingTimeInMillis(): Int = 0
    fun getMemoryLimitInMB(): Int = 0
    fun getLogger(): Logger = object : Logger {
        override fun log(text: String) {
            println(text)
        }
    }
}