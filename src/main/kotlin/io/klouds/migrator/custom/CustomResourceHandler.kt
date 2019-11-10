package io.klouds.migrator.custom

import com.amazonaws.services.lambda.runtime.Context
import com.fasterxml.jackson.annotation.JsonProperty
import io.klouds.migrator.CallbackHandler
import io.klouds.migrator.defaultTransform
import io.klouds.migrator.migration.MigrationRequest
import io.klouds.migrator.migration.MigrationResponse
import java.lang.Exception

data class ResponseMessage(
    @JsonProperty("Migrations") val migrations: Int
)

class CustomResourceHandler(
    val invoker: Invoker<MigrationRequest, MigrationResponse> = MigrationLambdaInvoker(),
    val eventPublisher: EventPublisher<CustomResourceResponse<ResponseMessage>, String> = StackEventPublisher()
) : CallbackHandler<CustomResourceRequest>(defaultTransform()) {

    override fun Context.handle(request: CustomResourceRequest): String {
        val response = responseBuilderFor(request)
        val responseUrl = request.responseUrl
        if (request.requestType == RequestType.Delete) {
            eventPublisher.publish(response(Status.SUCCESS, 0, null), to = responseUrl)
        } else {
            try {
                val result = migrate(request.asMigrationRequest())
                eventPublisher.publish(response(result.success, result.migrations, result.information), to = responseUrl)
            } catch (e: Exception) {
                logger.log(e.message)
                eventPublisher.publish(response(Status.FAILED, 0, e.message), to = responseUrl)
            }
        }
        return "Done"
    }

    private fun Context.responseBuilderFor(request: CustomResourceRequest) = { status: Status, migrations: Int, errorMessage: String? ->
        CustomResourceResponse(
                status, logStreamName, request.stackId, request.requestId, request.logicalResourceId,
                ResponseMessage(migrations), errorMessage
        )
    }

    private fun CustomResourceRequest.asMigrationRequest() = with(resourceProperties) {
        MigrationRequest(migrationBucket, migrationKey, databaseUrl, databaseUser, databaseSecretLocation, clean, schemas)
    }

    private fun Context.migrate(request: MigrationRequest): MigrationResponse {
        logger.log("Invoking $MIGRATOR_FUNCTION")
        logger.log(request.toString())
        val result = invoker.invoke(MIGRATOR_FUNCTION, request)
        logger.log("Migration Complete with result $result")
        return result
    }

    companion object {
        val MIGRATOR_FUNCTION = System.getenv("MIGRATOR_ARN").substringAfter("function:")
    }
}