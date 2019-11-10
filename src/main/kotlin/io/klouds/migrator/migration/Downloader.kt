package io.klouds.migrator.migration

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import io.klouds.migrator.Unzip
import io.klouds.migrator.Unzipper
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry

interface Downloader {
    fun download(source: String, name: String, destination: String)
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

    private fun s3ZipFrom(bucket: String, key: String) = s3Client.getObject(bucket, key).objectContent

    private fun writeTo(destination: File): ZipEntry.(ByteArray) -> Unit = { bytes ->
        if (isDirectory) File(destination, name).mkdirs()
        else FileOutputStream(File(destination, name)).use { it.write(bytes) }
    }

    override fun download(source: String, name: String, destination: String) {
        with(File(destination)) {
            clean()
            unzipper.unzip(s3ZipFrom(source, name), writeTo(this))
        }
    }
}