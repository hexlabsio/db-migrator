package io.klouds.migrator.migration

interface SecretFinder {
    fun secretFor(key: String): String
}