package com.ai.egret.utils


import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Creates MultipartBody.Part list named "files" expected by backend.
 * The backend expects form field name "files".
 */
fun uriListToMultipartParts(context: Context, uris: List<Uri>): List<MultipartBody.Part> {
    val parts = mutableListOf<MultipartBody.Part>()
    uris.forEachIndexed { index, uri ->
        val tmpFile = uriToTempFile(context, uri, "upload_${index}.jpg")
        val reqFile: RequestBody = tmpFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("files", tmpFile.name, reqFile)
        parts.add(part)
    }
    return parts
}

private fun uriToTempFile(context: Context, uri: Uri, filename: String): File {
    val tmpFile = File(context.cacheDir, filename)
    context.contentResolver.openInputStream(uri).use { input ->
        FileOutputStream(tmpFile).use { output ->
            input?.copyTo(output)
        }
    }
    return tmpFile
}

fun uriToMultipart(
    context: Context,
    uri: Uri,
    partName: String
): MultipartBody.Part {
    val inputStream = context.contentResolver.openInputStream(uri)!!
    val bytes = inputStream.readBytes()

    val body = bytes.toRequestBody("application/pdf".toMediaType())
    return MultipartBody.Part.createFormData(partName, "soil_report.pdf", body)
}
