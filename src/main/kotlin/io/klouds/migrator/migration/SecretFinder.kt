package io.klouds.migrator.migration

import com.amazonaws.regions.Regions
import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest
import com.fasterxml.jackson.module.kotlin.readValue
import io.klouds.migrator.Handler.Companion.objectMapper

interface SecretFinder {
    fun secretFor(key: String): String
}

class AwsSecretFinder(
    private val secretsManager: AWSSecretsManager = AWSSecretsManagerClient.builder().withRegion(Regions.EU_WEST_1).build()
) : SecretFinder {
    override fun secretFor(key: String) =
        objectMapper.readValue<Secret>(secretsManager.getSecretValue(GetSecretValueRequest().withSecretId(key)).secretString).password
}

data class Secret(val password: String)