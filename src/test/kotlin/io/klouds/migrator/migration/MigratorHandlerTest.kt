package io.klouds.migrator.migration

import io.klouds.migrator.TestContext
import io.klouds.migrator.custom.Status
import org.junit.jupiter.api.Test
import kotlin.test.expect

class MigratorHandlerTest {

    class TestDownloader(var source: String = "", var name: String = "", var destination: String = "") : Downloader {
        override fun download(source: String, name: String, destination: String) {
            this.source = source
            this.name = name
            this.destination = destination
        }
    }

    class TestSecretFinder(var key: String = "") : SecretFinder {
        override fun secretFor(key: String): String {
            this.key = key
            return "SuperSecret"
        }
    }

    class TestMigrator(private val fakeResponse: MigrationResponse, var dataSourceUrl: String = "", var username: String = "", var pssword: String = "") : Migrator<MigrationResponse> {
        override fun migrate(dataSourceUrl: String, username: String, password: String, schemas: List<String>, clean: Boolean): MigrationResponse {
            this.dataSourceUrl = dataSourceUrl
            this.username = username
            this.pssword = password
            return fakeResponse
        }
    }

    @Test
    fun `should download and invoke migrations with secret`() {
        val fakeResponse = MigrationResponse(Status.SUCCESS, 10)
        val migrationRequest = MigrationRequest("Bucket", "Key", "DatabaseUrl", "Username", "db/location")
        val testDownloader = TestDownloader()
        val testSecretFinder = TestSecretFinder()
        val testMigrator = TestMigrator(fakeResponse)
        expect(fakeResponse) { with(MigratorHandler(testDownloader, testSecretFinder, testMigrator)) {
            TestContext().handle(migrationRequest)
        } }
        with(testDownloader) {
            expect("Bucket") { source }
            expect("Key") { name }
            expect("/tmp/db/migration") { destination }
        }
        with(testSecretFinder) {
            expect("db/location") { key }
        }
        with(testMigrator) {
            expect("DatabaseUrl") { dataSourceUrl }
            expect("Username") { username }
            expect("SuperSecret") { pssword }
        }
    }
}