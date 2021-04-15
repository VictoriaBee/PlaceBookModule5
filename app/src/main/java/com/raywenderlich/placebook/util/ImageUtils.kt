package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

// 1 - Declared as an obj, so it behaves like a singleton; lets you directly call the
// methods with ImageUtils without creating new ImageUtils obj each time.
object ImageUtils {
    // 2 - Takes in a Context, Bitmap and String obj filename
    // and saves Bitmap to permanent storage.
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String) {
        // 3 - Created to hold the image data.
        val stream = ByteArrayOutputStream()
        // 4 - Writes the image bitmap to stream obj using the lossless PNG format.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        // 5 - stream is converted to write the bytes to a file.
        val bytes = stream.toByteArray()
        // 6 - saveBytesToFile() is called to write bytes to a file.
        ImageUtils.saveBytesToFile(context, bytes, filename)
    }

    // Passes context and filename and returns a Bitmap image
    // by loading image from specified filename.
    fun loadBitmapFromFile(context: Context, filename: String): Bitmap? {
        val filePath = File(context.filesDir, filename).absolutePath
        // Does the work of loading the image from the file
        // and the image is returned to the caller.
        return BitmapFactory.decodeFile(filePath)
    }

    // 7 - saveBytesToFile() takes in Context, ByteArray,
    // and String obj filename and saves the bytes to a file.
    private fun saveBytesToFile(context: Context, bytes: ByteArray, filename: String) {
        val outputStream: FileOutputStream
        // 8 - May throw exceptions; wrapped in try/catch to prevent crash.
        try {
            // 9 - openFileOutput used to open FileOutputStream using the given filename.
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE)
            // 10 - bytes are written to outputStream and then stream is closed.
            outputStream.write(bytes)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}