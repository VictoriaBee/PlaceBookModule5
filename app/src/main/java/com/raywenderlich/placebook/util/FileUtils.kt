package com.raywenderlich.placebook.util

import android.content.Context
import java.io.File

object FileUtils {

    // Deletes the image associated with a deleted bookmark.
    fun deleteFile(context: Context, filename: String) {
        val dir = context.filesDir
        val file = File(dir, filename)
        file.delete()
    }
}