package com.starry.greenstash.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.max

object ImageUtils {
    private const val TAG = "ImageUtils"

    fun createIconVector(name: String): ImageVector? {
        return try {
            val className = "androidx.compose.material.icons.filled.${name}Kt"
            val clazz = Class.forName(className)
            val method = clazz.declaredMethods.firstOrNull { it.returnType == ImageVector::class.java }
                ?: return null
            method.invoke(null, Icons.Filled) as? ImageVector
        } catch (error: Exception) {
            Log.w(TAG, "Unable to create icon vector: $name", error)
            null
        }
    }

    fun uriToBitmap(uri: Uri, context: Context, maxSize: Int): Bitmap? {
        if (maxSize <= 0) return null
        return runCatching {
            context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        }.getOrNull()?.let { compressBitmap(it, maxSize) }
    }

    private fun compressBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        if (bitmap.width <= maxSize && bitmap.height <= maxSize) return bitmap
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val width: Int
        val height: Int
        if (ratio > 1f) {
            width = maxSize
            height = max(1, (maxSize / ratio).toInt())
        } else {
            height = maxSize
            width = max(1, (maxSize * ratio).toInt())
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
}
