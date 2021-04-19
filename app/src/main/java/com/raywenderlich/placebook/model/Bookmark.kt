package com.raywenderlich.placebook.model

import android.content.Context
import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.raywenderlich.placebook.util.FileUtils
import com.raywenderlich.placebook.util.ImageUtils

// 1 - Tells Room that this is a db entity class.
@Entity
// 2 - The primary constructor is defined using arguments for all properties
//      with default values defined.
data class Bookmark(
    // 3 - id property is defined; autoGenerate attribute tells Room to automatically
    //      generate incrementing numbers for this field.
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    // 4 - Defined values means able to construct bookmark with
    //      partial list of properties.
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = "",
    var notes: String = "",
    var category: String = ""
)
{
    // 1 - Automatically generates filename for the bitmap that matches the bookmark ID;
    //      setImage() provides the public interface for saving an image for a Bookmark.
    fun setImage(image: Bitmap, context: Context) {
        // 2 - If bookmark has an id, then the image gets saved to a file;
        //      filename incorporates bookmark ID to make sure it's unique.
        id?.let {
            ImageUtils.saveBitmapToFile(context, image,
                generateImageFilename(it))
        }
    }

    // Deletes the image file associated with the current bookmark.
    fun deleteImage(context: Context) {
        id?.let {
            FileUtils.deleteFile(context, generateImageFilename(it))
        }
    }
    // 3 - generateImageFilename() is placed in a companion object;
    //      allows another obj to load image without having to load bookmark from db.
    companion object {
        fun generateImageFilename(id: Long): String {
            // 4 - Returns filename based on a Bookmark ID.
            return "bookmark$id.png"
        }
    }

    // Adds a category property.
    data class Bookmark(
        @PrimaryKey(autoGenerate = true) var id: Long? = null,
        var placeId: String? = null,
        var name: String = "",
        var address: String = "",
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var phone: String = "",
        var notes: String = "",
        var category: String = "")
}