package io.klouds.migrator.migration

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import io.klouds.migrator.Unzip
import io.klouds.migrator.Unzipper
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.RuntimeException
import java.security.MessageDigest
import java.util.Base64
import java.util.zip.ZipEntry

interface Downloader {
    fun download(source: String, name: String, destination: String, hash: String? = null)
}

class S3ZipDownloader(
    val s3Client: AmazonS3 = AmazonS3Client.builder().build(),
    val unzipper: Unzipper = Unzip
) : Downloader {

    private fun File.clean() {
        if (exists()) {
            deleteRecursively()
        }
        mkdirs()
    }

    private fun s3ZipFrom(bucket: String, key: String, hash: String? = null): InputStream {
        val content = s3Client.getObject(bucket, key).objectContent
        return if (hash != null) {
            val bytes = content.readBytes()
            val digest = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(content.readBytes()))
            println(hash)
            println(digest)
            if (hash == digest) {
                ByteArrayInputStream(bytes)
            } else {
                throw RuntimeException("Digest does not match content")
            }
        } else content
    }

    private fun writeTo(destination: File): ZipEntry.(ByteArray) -> Unit = { bytes ->
        if (isDirectory) File(destination, name).mkdirs()
        else FileOutputStream(File(destination, name)).use { it.write(bytes) }
    }

    override fun download(source: String, name: String, destination: String, hash: String?) {
        with(File(destination)) {
            clean()
            unzipper.unzip(s3ZipFrom(source, name, hash), writeTo(this))
        }
    }
}