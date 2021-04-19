package com.raywenderlich.placebook.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.raywenderlich.placebook.model.Bookmark

// 1 - Tells Room this is a Database class; stores a single entity type of Bookmark.
@Database(entities = arrayOf(Bookmark::class), version = 3)
abstract class PlaceBookDatabase : RoomDatabase() {
    // 2 - Defined to return a DAO interface; Room takes care of implementing the actual
    //      BookmarkDao class.
    abstract fun bookmarkDao(): BookmarkDao
    // 3 - Defines companion object on PlaceBookDatabase.
    companion object {
        // 4 - Defines the only instance variable on the companion object.
        private var instance: PlaceBookDatabase? = null
        // 5 - Defines getInstance() to take in a Context and returns single
        //      PlaceBookDatabase instance.
        fun getInstance(context: Context): PlaceBookDatabase {
            if (instance == null) {
                // 6 - First time getInstance is called, creates single PlaceBookDatabase instance.
                //       Room.databaseBuilder() used to create Room Database based on abstract class.
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceBookDatabase::class.java,
                    "PlaceBook")
                    // Tells Room to create new empty database if it
                    // can't find any Migrations.
                    .fallbackToDestructiveMigration()
                    .build()
            }
            // 7 - Returns PlaceBookDatabase instance.
            return instance as PlaceBookDatabase
        }
    }
}