package com.raywenderlich.placebook.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*

class BookmarkDetailsActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        // When bookmark details Activity starts, it processes the Intent
        // data passed in from the maps Activity.
        getIntentData()
    }

    private val bookmarkDetailsViewModel by
            viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView:
            BookmarkDetailsViewModel.BookmarkDetailsView? = null

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }

    // To populate the fields in the View.
    private fun populateFields() {
        bookmarkDetailsView?.let { bookmarkView ->
            editTextName.setText(bookmarkView.name)
            editTextPhone.setText(bookmarkView.phone)
            editTextNotes.setText(bookmarkView.notes)
            editTextAddress.setText(bookmarkView.address)
        }
    }

    // Loads the image from bookmarkView
    // and then uses it to set the imageViewPlace.
    private fun populateImageView() {
        bookmarkDetailsView?.let { bookmarkView ->
            val placeImage = bookmarkView.getImage(this)
            placeImage?.let {
                imageViewPlace.setImageBitmap(placeImage)
            }
        }

    }

    // Reads Intent data and uses it to populate the UI.
    private fun getIntentData() {
        // 1 - Pulls the BookmarkDetailsView from BookmarkDetailsViewModel
        // and then observe it for changes.
        val bookmarkId = intent.getLongExtra(
            MapsActivity.Companion.EXTRA_BOOKMARK_ID, 0
        )
        // 2 - Retrieves the BookmarkDetailsView from BookmarkDetailsViewModel
        // and then observes for changes.
        bookmarkDetailsViewModel.getBookmark(bookmarkId)?.observe(
            this,
            Observer<BookmarkDetailsViewModel.BookmarkDetailsView> {
                // 3 - Whenever BookmarkDetailsView is loaded or changed,
                // assigns the bookmarkDetailsView property to it,
                // and populates the bookmark fields from the data.
                it?.let {
                    bookmarkDetailsView = it
                    // Populates fields from bookmark
                    populateFields()
                    populateImageView()
                }
            }
        )
    }
}