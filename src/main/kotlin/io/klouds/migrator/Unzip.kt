package io.klouds.migrator

import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

interface Unzipper {
    fun unzip(inputStream: InputStream, fileFound: ZipEntry.(ByteArray) -> Unit)
}

object Unzip : Unzipper {
    override fun unzip(inputStream: InputStream, fileFound: (ZipEntry, ByteArray) -> Unit) {
        ZipInputStream(inputStream).use {
            var zipEntry = it.nextEntry
            while (zipEntry != null) {
                fileFound(zipEntry, it.readBytes())
                zipEntry = it.nextEntry
            }
            it.closeEntry()
        }
    }
}