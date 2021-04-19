package com.raywenderlich.placebook.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

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

    // Returns an empty File in app's private pictures folder using unique filename.
    @Throws(IOException::class)
    fun createUniqueImageFile(context: Context): File {
        val timeStamp =
            SimpleDateFormat("yyyMMddHHmmss").format(Date())
        val filename = "PlaceBook_" + timeStamp + "_"
        val filesDir = context.getExternalFilesDir(
            Environment.DIRECTORY_PICTURES)
        return File.createTempFile(filename, ".jpg", filesDir)
    }

    // Used to calculate the optimum inSampleSize that can be used to resize an image
    // to a specified width and height.
    private fun calculateInSampleSize(
        width: Int, height: Int,
        reqWidth: Int, reqHeight: Int): Int {

        // Starts with an inSampleSize of 1
        var inSampleSize = 1

        // and increases by a power of 2 until it reaches a value that will cause the
        // image to be downsampled to no larger than the requested image width and height.
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            } }
        return inSampleSize
    }

    fun decodeFileToSize(filePath: String,
        width: Int, height: Int): Bitmap {
        // 1 - Size of image is loaded using BitmapFactory.decodeFile().
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        // 2 - calculateInSampleSize() is called with the image width and height and the
        // requested width and height;
        // Options is updated with the resulting inSampleSize.
        options.inSampleSize = calculateInSampleSize(
            options.outWidth, options.outHeight, width, height)
        // 3 - inJustDecodeBounds is set to false to load the full image this time.
        options.inJustDecodeBounds = false
        // 4
        return BitmapFactory.decodeFile(filePath, options)
    }

    fun decodeUriStreamToSize(uri: Uri,
        width: Int, height: Int, context: Context): Bitmap? {
        var inputStream: InputStream? = null
        try {
            val options: BitmapFactory.Options
            // 1 - inputStream is opened for the Uri.
            inputStream = context.contentResolver.openInputStream(uri)
            // 2 - If inputStream is not null, processing continues.
            if (inputStream != null) {
                // 3 - The image size is determined.
                options = BitmapFactory.Options()
                options.inJustDecodeBounds = false
                BitmapFactory.decodeStream(inputStream, null, options)
                // 4 - The input stream is closed and opened again and checked for null.
                inputStream.close()
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    // 5 - Image is loaded from the stream using the downsampling options
                    // and is returned to the caller.
                    options.inSampleSize = calculateInSampleSize(
                        options.outWidth, options.outHeight,
                        width, height)
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(
                        inputStream, null, options)
                    inputStream.close()
                    return bitmap
                }
            }
            return null
        }   catch (e: Exception) {
            return null
        }   finally {
            // 6 - Closes inputStream.
            inputStream?.close()
        }
    }

}