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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// 1 - Inherits from AndroidViewModel; allows to include the application context
//      which is needed when creating the BookmarkRepo.
class MapsViewModel(application: Application) :
        AndroidViewModel(application) {

    private var bookmarks: LiveData<List<BookmarkView>>? = null
    private val TAG = "MapsViewModel"
    // 2 - Creates the BookmarkRepo obj, passing in on the app context.
    private var bookmarkRepo: BookmarkRepo = BookmarkRepo(getApplication())

    // Helper method that converts a Bookmark object from the repo into a BookMarkerView obj.
    private fun bookmarkToBookmarkView(bookmark: Bookmark):
            MapsViewModel.BookmarkView {
        return MapsViewModel.BookmarkView(
            bookmark.id,
            LatLng(bookmark.latitude, bookmark.longitude),
            bookmark.name,
            bookmark.phone,
            bookmarkRepo.getCategoryResourceId(bookmark.category))
    }
    // Used by previous method.
    private fun mapsBookmarksToBookmarkView() {
        // 1
        bookmarks = Transformations.map(bookmarkRepo.allBookmarks)
        { repoBookmarks ->
            // 2
            repoBookmarks.map { bookmark ->
                bookmarkToBookmarkView(bookmark)
            }
        }
    }

    //Converts a place type to a bookmark category.
    private fun getPlaceCategory(place: Place): String {
        // 1 - If no type assigned to place, it defaults to "Other".
        var category = "Other"
        val placeTypes = place.types

        placeTypes?.let { placeTypes ->
            // 2 - Checks the placeTypes List to see if it's populated.
            if (placeTypes.size > 0) {
                // 3 - If populated, it extracts first type from List and calls
                // placeTypeToCategory() to make the conversion.
                val placeType = placeTypes[0]
                category = bookmarkRepo.placeTypeToCategory(placeType)
            }
        }
        // 4 - Returns the category.
        return category
    }

    // Returns the LiveData obj that will be observed by MapsActivity.
    fun getBookmarkViews() :
        LiveData<List<BookmarkView>>? {
        if (bookmarks == null) {
            mapsBookmarksToBookmarkView()
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
        // Assigns category to the newly created bookmark.
        bookmark.category = getPlaceCategory(place)
        // 5 - Saves the Bookmark to the repo and prints info message to verify bookmark was added.
        val newId = bookmarkRepo.addBookmark(bookmark)
        // Setting the image for a bookmark when it's added to the db.
        image?.let {
            bookmark.setImage(it, getApplication())
        }

        Log.i(TAG, "New bookmark $newId added to the database.")
    }

    // Ad-Hoc bookmark; takes in a LatLng location and creates new untitled bookmark
    // at the given location; returns new bookmarkID to the caller.
    fun addBookmark(latLng: LatLng) : Long? {
        val bookmark = bookmarkRepo.createBookmark()
        bookmark.name="Untitled"
        bookmark.longitude = latLng.longitude
        bookmark.latitude = latLng.latitude
        bookmark.category = "Other"
        return bookmarkRepo.addBookmark(bookmark)
    }

    // Holds the info needed by the View to plot a marker for a single bookmark.
    data class BookmarkView(
        val id: Long? = null,
        val location: LatLng = LatLng(0.0, 0.0),
        val name: String = "",
        val phone: String = "",
        val categoryResourceId: Int? = null) {
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