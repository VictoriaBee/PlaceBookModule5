package com.raywenderlich.placebook.db

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import com.raywenderlich.placebook.model.Bookmark

// 1 - Tells Room this is a Data Access Object.
@Dao
interface BookmarkDao {

    // 2 - loadAll() uses @Query to define SQL statement to read all bookmarks
    //      from db and return them as List of Bookmarks.
    @Query("SELECT * FROM Bookmark ORDER BY name")
    fun loadAll(): LiveData<List<Bookmark>>

    // 3 - Returns a single Bookmark object.
    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadBookmark(bookmarkId: Long): Bookmark
    // Async version that returns a LiveData wrapper around single bookkmark.
    @Query("SELECT * FROM Bookmark WHERE id = :bookmarkId")
    fun loadLiveBookmark(bookmarkId: Long): LiveData<Bookmark>

    // 4 - Saves single Bookmark to the database and returns the new primary key
    //      id associated with the new bookmark.
    @Insert(onConflict = IGNORE)
    fun insertBookmark(bookmark: Bookmark): Long

    // 5 - Updates a single Bookmark in the db using the passed in bookmark argument.
    //      Existing bookmark is db is replaced with new bookmark data.
    @Update(onConflict = REPLACE)
    fun updateBookmark(bookmark: Bookmark)

    // 6 - Deletes an existing bookmark based on the passed in Bookmark.
    @Delete
    fun deleteBookmark(bookmark: Bookmark)

}