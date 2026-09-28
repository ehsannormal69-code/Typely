package com.typely.keyboard.ui

import android.content.Context
import android.net.Uri
import java.io.File

class BackgroundImageStore(private val context: Context) {

    private val dir = File(context.filesDir, "backgrounds").apply { mkdirs() }

    fun import(uri: Uri): String? {
        return runCatching {
            val target = File(dir, "background_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            target.absolutePath
        }.getOrNull()
    }

    fun remove() {
        dir.listFiles()?.forEach { it.delete() }
    }
}