package com.raywenderlich.placebook.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.raywenderlich.placebook.model.Bookmark
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.util.ImageUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        var notes: String = "",
        var category: String = "",
        var longitude: Double = 0.0,
        var latitude: Double = 0.0,
        var placeId: String? = null) {

        // Loads the image associated with the bookmark.
        fun getImage(context: Context): Bitmap? {
            id?.let {
                return ImageUtils.loadBitmapFromFile(context,
                    Bookmark.generateImageFilename(it))
            }
            return null
        }

        // Takes in Bitmap image and saves it to
        // associated image file for the current BookmarkView.
        fun setImage(context: Context, image: Bitmap) {
            id?.let {
                ImageUtils.saveBitmapToFile(context, image,
                    Bookmark.generateImageFilename(it))
            }
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
            bookmark.notes,
            bookmark.category,
            bookmark.longitude,
            bookmark.latitude,
            bookmark.placeId)
    }

    // Converts from a live db bookmark obj to a live bookmark view obj.
    private fun mapBookmarkToBookmarkView(bookmarkId: Long) {
        val bookmark = bookmarkRepo.getLiveBookmark(bookmarkId)
        bookmarkDetailsView = Transformations.map(bookmark)
        { repoBookmark ->
            repoBookmark?.let { repoBookmark ->
                bookmarkToBookmarkView(repoBookmark)
            }
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

    // Converts a bookmark view model to the db bookmark model
    // for when the user changes to a bookmark.
    private fun bookmarkViewToBookmark(bookmarkView:
    BookmarkDetailsView): Bookmark? {
        val bookmark = bookmarkView.id?.let {
            bookmarkRepo.getBookmark(it)
        }
        if (bookmark != null) {
            bookmark.id = bookmarkView.id
            bookmark.name = bookmarkView.name
            bookmark.phone = bookmarkView.phone
            bookmark.address = bookmarkView.address
            bookmark.notes = bookmarkView.notes
            bookmark.category = bookmarkView.category
        }
        return bookmark
    }

    fun updateBookmark(bookmarkView: BookmarkDetailsView) {
        // 1 - A coroutine is used to run method in background.
        //    Allows calls to be made by the bookmark repo that accesses the db.
        GlobalScope.launch {
            // 2 - The BookmarkDetailsView is converted to a Bookmark.
            val bookmark = bookmarkViewToBookmark(bookmarkView)
            // 3 - If bookmark is not null, updated in bookmark repo;
            //      this updates bookmark in the db.
            bookmark?.let { bookmarkRepo.updateBookmark(it) }
        }
    }

    // Takes in a BookmarkDetailsView and loads the bookmark from the repo.
    // If found, calls deleteBookmark() on the repo.
    fun deleteBookmark(bookmarkDetailsView: BookmarkDetailsView) {
        GlobalScope.launch {
            val bookmark = bookmarkDetailsView.id?.let {
                bookmarkRepo.getBookmark(it)
            }
            bookmark?.let {
                bookmarkRepo.deleteBookmark(it)
            }
        }
    }

    fun getCategoryResourceId(category: String): Int? {
        return bookmarkRepo.getCategoryResourceId(category)
    }

    fun getCategories(): List<String> {
        return bookmarkRepo.categories
    }
 }