package io.klouds.migrator.migration

import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest

interface SecretFinder {
    fun secretFor(key: String): String
}

class AwsSecretFinder(
    private val secretsManager: AWSSecretsManager = AWSSecretsManagerClient.builder().build()
) : SecretFinder {
    override fun secretFor(key: String) =
        secretsManager.getSecretValue(GetSecretValueRequest().withSecretId(key)).secretString
}