package com.segotlo.campusfindapp.util

import android.content.Context
import android.net.Uri
import java.io.File

object UriUtils {

    /**
     * Copies a content URI (e.g. from Photo Picker or GetContent) into a local file in app cache.
     * Photo Picker URIs grant temporary read permissions that expire or cannot be persisted.
     * Copying to internal cache ensures persistent access without SecurityExceptions.
     *
     * @return a safe `file://` scheme Uri if successfully copied, or `null` if copy failed or URI invalid.
     */
    fun copyUriToInternalFile(context: Context, uri: Uri, prefix: String = "media"): Uri? {
        if (uri.scheme == "file") {
            return uri
        }

        return try {
            val fileName = "${prefix}_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (file.exists() && file.length() > 0) {
                Uri.fromFile(file)
            } else {
                null
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
