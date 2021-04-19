package com.raywenderlich.placebook.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.util.ImageUtils
import com.raywenderlich.placebook.viewmodel.BookmarkDetailsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import kotlinx.android.synthetic.main.activity_bookmark_details.toolbar
import kotlinx.android.synthetic.main.content_bookmark_info.*
import kotlinx.android.synthetic.main.main_view_maps.*
import java.io.File
import java.net.URLEncoder

class BookmarkDetailsActivity  : AppCompatActivity(),
        PhotoOptionsDialogFragment.PhotoOptionDialogListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark_details)
        setupToolbar()
        // When bookmark details Activity starts, it processes the Intent
        // data passed in from the maps Activity.
        getIntentData()
        setupFab()
    }

    // Overrides onCreateOptionsMenu and provides items for the
    // Toolbar by loading in the menu_bookmark_details menu.
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_bookmark_details, menu)
        return true
    }

    private val bookmarkDetailsViewModel by
            viewModels<BookmarkDetailsViewModel>()
    private var bookmarkDetailsView:
            BookmarkDetailsViewModel.BookmarkDetailsView? = null
    // Reference to the temporary image file when capturing an image.
    private var photoFile: File? = null

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
            // Sets click listener on imageViewPlace and
            // calls replaceImage() when image is tapped.
            imageViewPlace.setOnClickListener { replaceImage() }
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
                    populateCategoryList()
                }
            })
    }

    // Takes current changes from the text fields and updates the bookmark.
    private fun saveChanges() {
        val name = editTextName.text.toString()
        if (name.isEmpty()) {
            return
        }
        bookmarkDetailsView?.let { bookmarkView ->
            // After updating bookmarkView with data from the EditText fields,
            // updateBookmark() is called to update bookmark model.
            bookmarkView.name = editTextName.text.toString()
            bookmarkView.notes = editTextNotes.text.toString()
            bookmarkView.address = editTextAddress.text.toString()
            bookmarkView.phone = editTextPhone.text.toString()
            bookmarkView.category = spinnerCategory.selectedItem as String
            bookmarkDetailsViewModel.updateBookmark(bookmarkView)
        }
        // Activity is closed.
        finish()
    }

    override fun onCaptureClick() {
        // 1 - Any previously assigned photoFile is cleared.
        photoFile = null
        try {
            // 2 - Calls createUniqueImageFile() to create a
            // uniquely names image File and assigns it to photoFile.
            photoFile = ImageUtils.createUniqueImageFile(this)
        }   catch (ex: java.io.IOException) {
            // 3 - If exception is thrown, the method returns without doing anything.
            return
        }
        // 4 "?.let" makes sure photoFile is not null before continuing with method.
        photoFile?.let { photoFile ->
            // 5 - Called to get a Uri for temporary photo file.
            val photoUri = FileProvider.getUriForFile(this,
                "com.raywenderlich.placebook.fileprovider",
                photoFile)
            // 6 - New Intent is created; used to display the camera viewfinder
            // and allow user to snap a new photo.
            val captureIntent =
                Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            // 7 - photoUri is added as an extra on Intent, so Intent
            // knows where to save full-size image captured by the user.
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                photoUri)
            // 8 - Temporary write permissions on the photoUri are given to the Intent.
            val intentActivities = packageManager.queryIntentActivities(
                captureIntent, PackageManager.MATCH_DEFAULT_ONLY)
            intentActivities.map { it.activityInfo.packageName }
                .forEach { grantUriPermission(it, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION) }
            // 9 - Intent is invoked, and request code is passed in.
            startActivityForResult(captureIntent, REQUEST_CAPTURE_IMAGE)
        }
    }

    override fun onPickClick() {
        // Kicks off Android's image selection activity.
        val pickIntent = Intent(Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickIntent, REQUEST_GALLERY_IMAGE)
    }

    // When user taps on bookmark image.
    private fun replaceImage() {
        val newFragment = PhotoOptionsDialogFragment.newInstance(this)
        newFragment?.show(supportFragmentManager, "photoOptionsDialog")
    }

    // Called when user selects Toolbar checkmark item.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                saveChanges()
                return true
            }
            R.id.action_delete -> {
                deleteBookmark()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // Assigns an image to the imageViewPlace and saves it to the bookmark
    // image file using bookmarkDetailsView.setImage().
    private fun updateImage(image: Bitmap) {
        val bookmarkView = bookmarkDetailsView ?: return
        imageViewPlace.setImageBitmap(image)
        bookmarkView.setImage(this, image)
    }

    // Uses new decodeFileSize method to load the downsampled image
    // and returns it.
    private fun getImageWithPath(filePath: String): Bitmap? {
        return ImageUtils.decodeFileToSize(filePath,
            resources.getDimensionPixelSize(
                R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                R.dimen.default_image_height))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 1 - resultCode is checked to make sure user didn't cancel photo capture.
        if (resultCode == android.app.Activity.RESULT_OK) {
            // 2 - Checked to see which call is returning a result.
            when (requestCode) {
                // 3 - If requestCode matches REQUEST_CAPTURE_IMAGE,
                // then processing continues.
                REQUEST_CAPTURE_IMAGE -> {
                    // 4 - Returns early from the method if there
                    // is no photoFile defined.
                    val photoFile = photoFile ?: return
                    // 5 - Permissions are now revoked since they're no longer needed.
                    val uri = FileProvider.getUriForFile(this,
                        "com.raywenderlick.placebook.fileprovider",
                        photoFile)
                    revokeUriPermission(uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    // 6 - Called to get the image from the new photo path.
                    val image = getImageWithPath(photoFile.absolutePath)
                    // Called to update the bookmark image.
                    image?.let { updateImage(it)}
                }
                // If Activity result is from selecting a gallery image, and the data
                // returned is valid,
                REQUEST_GALLERY_IMAGE ->
                    if (data != null && data.data != null) {
                        val imageUri = data.data as Uri
                        // then getImageWithAuthority() is called to load the selected image.
                        val image = getImageWithAuthority(imageUri)
                        // Called to update bookmark image.
                        image?.let { updateImage(it) }
                    }
            }
        }
    }

    // Uses the decodeUriStreamToSize to load the downsampled image and return it.
    private fun getImageWithAuthority(uri: Uri): Bitmap? {
        return ImageUtils.decodeUriStreamToSize(uri,
            resources.getDimensionPixelSize(
                R.dimen.default_image_width),
            resources.getDimensionPixelSize(
                R.dimen.default_image_height),
            this)
    }

    private fun populateCategoryList() {
        // 1 - Returns immediately if bookmarkDetailsView is null.
        val bookmarkView = bookmarkDetailsView ?: return
        // 2 - Retrieves category icon resourceId from the view model.
        val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(
            bookmarkView.category)

        // 3 - If resourceId is not null, updates imageViewCategory
        // to category icon.
        resourceId?.let { imageViewCategory.setImageResource(it) }
        // 4 - Retrieves the list of categories from the view model.
        val categories = bookmarkDetailsViewModel.getCategories()
        // 5 - Creates an Adapter; simple ArrayAdapter built
        // from the list of category names.
        val adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, categories)
        // setDropDownViewResource() assigns the Adapter to
        // a standard built-in Layout resource.
        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        // 6 - Assigns the Adapter to the spinnerCategory control.
        spinnerCategory.adapter = adapter
        // 7 - Updates spinnerCategory to reflect the current category selection.
        val placeCategory = bookmarkView.category
        spinnerCategory.setSelection(
            adapter.getPosition(placeCategory))

        // 1 - spinnerCategory.post is needed since onItemSelected() is always
        // called once with an initial position of 0.
        spinnerCategory.post {
            // 2 - Assigns spinnerCategory onItemSelectedListener property to an
            // instance of onItemSelectedListener class that implements
            // onItemsSelected() and onNothingSelected().
            spinnerCategory.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // 3 - For when user selects new category.
                    val category = parent?.getItemAtPosition(position) as String
                    val resourceId = bookmarkDetailsViewModel.getCategoryResourceId(category)
                    resourceId?.let {
                        imageViewCategory.setImageResource(it) }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // NOTE: This method is required but not used.
                }
                }
        }
    }

    private fun deleteBookmark() {
        val bookmarkView = bookmarkDetailsView ?: return

        // Displays AlertDialog to ask user if they want to delete bookmark.
        AlertDialog.Builder(this)
            .setMessage("Delete?")
            // If select OK, deletes bookmark and Activity closes.
            .setPositiveButton("OK") { _, _ ->
                bookmarkDetailsViewModel.deleteBookmark(bookmarkView)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun sharePlace() {
        // 1 - Early return if bookmarkView is null.
        val bookmarkView = bookmarkDetailsView ?: return
        // 2 - Build out Google Maps URL to trigger driving directions
        // to bookmarked place.
        var mapUrl = ""
        if (bookmarkView.placeId == null) {
            // 3 - Allows command to work in the URL.
            val location = URLEncoder.encode("${bookmarkView.latitude}," +
                    "${bookmarkView.longitude}", "utf-8")
            mapUrl = "https://www.google.com/maps/dir/?api=1" + "&destingation=$location"
        } else {
            // 4 - For the option with place ID available,
            // the destination contains the place name.
            val name = URLEncoder.encode(bookmarkView.name, "utf-8")
            mapUrl = "https://google.com/maps/dir/?api=1" +
                    "&destination=$name&destination_place_id=" +
                    "${bookmarkView.placeId}"
        }
        // 5 - Creates the sharing Activity Intent and
        // sets the action to ACTION_SEND; tells Android it's meant
        // to share the data with another app on the device.
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        // 6 - The app that receives the Intent can choose what to use and ignore.
        sendIntent.putExtra(Intent.EXTRA_TEXT,
            "Check out ${bookmarkView.name} at:\n$mapUrl")
        sendIntent.putExtra(Intent.EXTRA_SUBJECT,
            "Sharing ${bookmarkView.name}")
        // 7 - "text/plain" tells Android to share plain text data.
        sendIntent.type = "text/plain"
        // 8 - Sharing Activity is started.
        startActivity(sendIntent)
    }

    private fun setupFab() {
        fab.setOnClickListener {
            sharePlace()
        }
    }

    // Defines request code to use when processing the camera capture Intent.
    companion object {
        private const val REQUEST_CAPTURE_IMAGE = 1
        private const val REQUEST_GALLERY_IMAGE = 2
    }
}