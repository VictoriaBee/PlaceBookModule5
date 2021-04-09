package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo

// 1 - Inherits from AndroidViewModel; allows to include the application context
//      which is needed when creating the BookmarkRepo.
class MapsViewModel(application: Application) :
        AndroidViewModel(application) {

    private var bookmarks: LiveData<List<BookMarkerView>>? = null
    private val TAG = "MapsViewModel"
    // 2 - Creates the BookmarkRepo obj, passing in on the app context.
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())

    // Helper method that converts a Bookmark object from the repo into a BookMarkerView obj.
    private fun bookmarkToMarkerView(bookmark: Bookmark):
            MapsViewModel.BookMarkerView {
        return MapsViewModel.BookMarkerView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude)
        )
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
        LiveData<List<BookMarkerView>>? {
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

        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    // Holds the info needed by the View to plot a marker for a single bookmark.
    data class BookMarkerView(
        var id: Long? = null,
        var location: LatLng = LatLng(0.0, 0.0))
}