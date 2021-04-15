package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

// 1 - Inherits from AndroidViewModel; allows to include the application context
//      which is needed when creating the BookmarkRepo.
class MapsViewModel(application: Application) :
        AndroidViewModel(application) {

    private var bookmarks: LiveData<List<BookmarkMarkerView>>? = null
    private val TAG = "MapsViewModel"
    // 2 - Creates the BookmarkRepo obj, passing in on the app context.
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())

    // Helper method that converts a Bookmark object from the repo into a BookMarkerView obj.
    private fun bookmarkToMarkerView(bookmark: Bookmark):
            MapsViewModel.BookmarkMarkerView {
        return MapsViewModel.BookmarkMarkerView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone)
    }
    // Used by previous method.
    private fun mapBookmarksToMarkerView() {
        // 1
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks)
        { repoBookmarks ->
            // 2
            repoBookmarks.map { bookmark ->
                bookmarkToMarkerView(bookmark)
            }
        }
    }

    // Returns the LiveData obj that will be observed by MapsActivity.
    fun getBookmarkMarkerViews() :
        LiveData<List<BookmarkMarkerView>>? {
        if (bookmarks == null) {
            mapBookmarksToMarkerView()
        }
        return bookmarks
    }

    // 3 - Declares the method addBookmarkFromPlace that takes in a Google Place
    //      and a Bitmap image.
    fun addBookmarkFromPlace(place: Place, image: Bitmap?) {
        // 4 - Uses BookmarkRepo.createBookmark() to create an empty Bookmark obj
        //      and fills it in using the Place data.
        val bookmark = bookmarkRepo.createBookmark()
            bookmark.placeId = place.id
            bookmark.name = place.name.toString()
            bookmark.longitude = place.latLng?.longitude ?: 0.0
            bookmark.latitude = place.latLng?.latitude ?: 0.0
            bookmark.phone = place.phoneNumber.toString()
            bookmark.address = place.address.toString()
        // 5 - Saves the Bookmark to the repo and prints info message to verify bookmark was added.
        val newId = bookmarkRepo.addBookmark(bookmark)
        // Setting the image for a bookmark when it's added to the db.
        image?.let {
            bookmark.setImage(it, getApplication())
        }

        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    // Holds the info needed by the View to plot a marker for a single bookmark.
    data class BookmarkMarkerView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0),
        var name: String = "",
        var phone: String = "") {
        // Provides the image for the View.
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context,
                    Bookmark.generateImageFilename(it))
            }
            return null
        }
    }
}