package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils

class BookmarkDetailsViewModel(application: Application):
        AndroidViewModel(application) {

    // Holds the LiveData<BookmarkDetailsView> object,
    // allows the View to stay updated anytime view model changes.
    private var bookmarkDetailsView: LiveData<BookmarkDetailsView>?
        = null

    // Private BookmarkRepo property is defined and
    // initialized with a new BookmarkRepo instance.
    private var bookmarkRepo: BookmarkRepo =
        BookmarkRepo(getApplication())

    // Defines the data needed by BookmarkDetailsActivity.
    data class BookmarkDetailsView(
        var id: Long? = null,
        var name: String = "",
        var phone: String = "",
        var address: String = "",
        var notes: String = ""  ) {

        // Loads the image associated with the bookmark.
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context,
                    Bookmark.generateImageFilename(it))
            }
            return null
        }
    }

    // Converts a Bookmark model to a BookmarkDetailsView model.
    private fun bookmarkToBookmarkView(bookmark: Bookmark):
            BookmarkDetailsView {
        return BookmarkDetailsView(
            bookmark.id,
            bookmark.name,
            bookmark.phone,
            bookmark.address,
            bookmark.notes)
    }

    // Converts from a live db bookmark obj to a live bookmark view obj.
    private fun mapBookmarkToBookmarkView(bookmarkId: Long) {
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark)
        { repoBookmark ->
            bookmarkToBookmarkView(repoBookmark)
        }
    }

    // Returning the live bookmark View based on a bookmark ID.
    fun getBookmark(bookmarkId: Long):
            LiveData<BookmarkDetailsView>? {
        if (bookmarkDetailsView == null) {
            mapBookmarkToBookmarkView(bookmarkId)
        }
        return bookmarkDetailsView
    }
 }