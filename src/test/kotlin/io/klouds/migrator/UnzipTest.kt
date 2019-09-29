package io.klouds.migrator

import org.junit.jupiter.api.Test
import java.util.zip.ZipEntry
import kotlin.test.expect

class UnzipTest {
    data class TestResults(var count: Int = 0, var content: MutableMap<String, String> = mutableMapOf(), var directories: MutableList<String> = mutableListOf()) {
        fun fileFound(entry: ZipEntry, bytes: ByteArray) {
            if (!entry.isDirectory) {
                count++
                content[entry.name] = String(bytes)
            } else {
                directories.add(entry.name)
            }
        }
    }

    @Test
    fun `should unzip single file correctly`() {
        with(TestResults()) {
            Unzip.unzip(UnzipTest::class.java.getResourceAsStream("/OneFileTest.zip"), ::fileFound)
            expect(1) { count }
            expect(0) { directories.size }
            expect("Hello World") { content["Test.txt"] }
        }
    }

    @Test
    fun `should unzip two files correctly`() {
        with(TestResults()) {
            Unzip.unzip(UnzipTest::class.java.getResourceAsStream("/TwoFilesTest.zip"), ::fileFound)
            expect(2) { count }
            expect(0) { directories.size }
            expect("First File") { content["One.txt"] }
            expect("Second File") { content["Two.txt"] }
        }
    }

    @Test
    fun `should unzip nested files correctly`() {
        with(TestResults()) {
            Unzip.unzip(UnzipTest::class.java.getResourceAsStream("/NestedTest.zip"), ::fileFound)
            expect(3) { count }
            expect(1) { directories.size }
            expect("First File") { content["One.txt"] }
            expect("Second File") { content["Two.txt"] }
            expect("Third File") { content["nested/Three.txt"] }
            expect("nested/") { directories.first() }
        }
    }
}