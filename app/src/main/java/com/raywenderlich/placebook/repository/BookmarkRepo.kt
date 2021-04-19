package com.raywenderlich.placebook.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.google.android.libraries.places.api.model.Place
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.db.BookmarkDao
import com.raywenderlich.placebook.db.PlaceBookDatabase
import com.raywenderlich.placebook.model.Bookmark

// 1 - Defines class with constructor that passes in an object named context.
class BookmarkRepo(private val context: Context) {
    // 2 - Properties BookmarkRepo will use for data source.
    private val db: PlaceBookDatabase = PlaceBookDatabase.getInstance(context)
    private val bookmarkDao: BookmarkDao = db.bookmarkDao()
    //Initializes categoryMap to hold mapping of place types to category names.
    private var categoryMap: HashMap<Place.Type, String> =
        buildCategoryMap()
    // Initializes allCategories to hold the mapping of
    // category names to resource IDs.
    private var allCategories: HashMap<String, Int> =
        buildCategories()
    val categories: List<String>
        get() = ArrayList(allCategories.keys)

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

    // Takes in a bookmark and saves it using the bookmark DOA.
    fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }

    // Takes in a bookmark ID and uses the bookmark DAO
    // to load the corresponding bookmark.
    fun getBookmark(bookmarkId: Long): Bookmark {
        return bookmarkDao.loadBookmark(bookmarkId)
    }

    // Deletes the bookmark image and the bookmark from the db.
    fun deleteBookmark(bookmark: Bookmark) {
        bookmark.deleteImage(context)
        bookmarkDao.deleteBookmark(bookmark)
    }

    // Builds a HashMap that relates Place types to category names.
    // Anything not included in this list will end up in the Other category.
    private fun buildCategoryMap() : HashMap<Place.Type, String> {
        return hashMapOf(
            Place.Type.BAKERY to "Restaurant",
            Place.Type.BAR to "Restaurant",
            Place.Type.CAFE to "Restaurant",
            Place.Type.FOOD to "Restaurant",
            Place.Type.RESTAURANT to "Restaurant",
            Place.Type.MEAL_DELIVERY to "Restaurant",
            Place.Type.MEAL_TAKEAWAY to "Restaurant",
            Place.Type.GAS_STATION to "Gas",
            Place.Type.CLOTHING_STORE to "Shopping",
            Place.Type.DEPARTMENT_STORE to "Shopping",
            Place.Type.FURNITURE_STORE to "Shopping",
            Place.Type.GROCERY_OR_SUPERMARKET to "Shopping",
            Place.Type.HARDWARE_STORE to "Shopping",
            Place.Type.HOME_GOODS_STORE to "Shopping",
            Place.Type.JEWELRY_STORE to "Shopping",
            Place.Type.SHOE_STORE to "Shopping",
            Place.Type.SHOPPING_MALL to "Shopping",
            Place.Type.STORE to "Shopping",
            Place.Type.LODGING to "Lodging",
            Place.Type.ROOM to "Lodging")
    }

    // Takes in Place type and converts it to a valid category.
    fun placeTypeToCategory(placeType: Place.Type): String {
        var category = "Other"
        // If categoryMap contains a key matching placeType,
        if (categoryMap.containsKey(placeType)) {
            // it's assigned to category.
            category = categoryMap[placeType].toString()
        }
        return category
    }

    // Builds a HashMap that related the category names to
    // the category icon resource IDs.
    private fun buildCategories() : HashMap<String, Int> {
        return hashMapOf(
            "Gas" to R.drawable.ic_gas,
            "Lodging" to R.drawable.ic_lodging,
            "Other" to R.drawable.ic_other,
            "Restaurant" to R.drawable.ic_restaurant,
            "Shopping" to R.drawable.ic_shopping)
    }

    // Provides a public method to convert a category name
    // to a resource ID.
    fun getCategoryResourceId(placeCategory: String): Int? {
        return allCategories[placeCategory]
    }
}