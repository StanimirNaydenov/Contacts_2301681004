package com.example.contacts
import android.content.*
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.*
import java.io.*
import java.util.UUID

class PhotoRepository(private val context: Context) {
    suspend fun saveFromUri(source: Uri): String = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            val src = ImageDecoder.createSource(resolver, source)
            ImageDecoder.decodeBitmap(src)
        } else {
            @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(resolver, source)
        }

        val fileName = "contact_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    }
}
