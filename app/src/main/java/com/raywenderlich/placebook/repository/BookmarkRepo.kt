package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

// 1 - Defines class with constructor that passes in an object named context.
class BookmarkRepo(context: Context) {
    // 2 - Properties BookmarkRepo will use for data source.
    private val db: PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()

    // 3 - Creates addBookmark() to allow a single Bookmark to be added to repo.
    fun addBookmark(bookmark: Bookmark): Long? {
        // Creates a unique id of the newly saved Bookmark or null if can't be saved.
        val newId = bookmarkDao.insertBookmark(bookmark)
        bookmark.id = newId
        return newId
    }

    // 4 - Adds createBookmark() as a helper method to return freshly initialized Bookmark object.
    fun createBookmark(): Bookmark {
        return Bookmark()
    }

    // 5 - Creates allBookmarks property that returns a LiveData list of all Bookmarks in the Repo.
    val allBookmarks: LiveData<List<Bookmark>>
        get() {
            return bookmarkDao.loadAll()
        }

    // Returns a live bookmark from the bookmark DAO.
    fun getLiveBookmark(bookmarkId: Long): LiveData<Bookmark> {
        val bookmark = bookmarkDao.loadLiveBookmark(bookmarkId)
        return bookmark
    }
}