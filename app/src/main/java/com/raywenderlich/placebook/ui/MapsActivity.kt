package com.raywenderlich.placebook.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.Autocomplete.getPlaceFromIntent
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.adapter.BookmarkListAdapter
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.activity_bookmark_details.*
import kotlinx.android.synthetic.main.activity_bookmark_details.toolbar
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.drawer_view_maps.*
import kotlinx.android.synthetic.main.main_view_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Private members.
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var markers = HashMap<Long, Marker>()
    private lateinit var bookmarkListAdapter: BookmarkListAdapter

    // Holds the MapsViewModel; initialized when map is ready.
    private val mapsViewModel by viewModels<MapsViewModel>()

    // Loads the activity_maps.xml Layout and finds the map Fragment from Layout
    // and uses initializes map with getMapAsync().
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Calls to be initialized.
        setupLocationClient()
        setupPlacesClient()
        setupToolbar()
        setupNavigationDrawer()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    // Called by the SupportMapFragment obj when map is ready to go.
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setupMapListeners()
        getCurrentLocation()
        createBookmarkObserver()
    }
    // Creating the PlacesClient.
    private fun setupPlacesClient() {
        Places.initialize(getApplicationContext(),       // Change syntax?
            getString(R.string.google_maps_key));
        placesClient = Places.createClient(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Checks if result matches REQUEST_LOCATION request code.
        if (requestCode == REQUEST_LOCATION) {
            // Checks if first item in grantResults array contains PERMISSION_GRANTED value.
            if(grantResults.size == 1 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                // If correct, get current location.
                getCurrentLocation()
            }   else {
                // No permission granted, so get error message.
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    // To use the fused location API.
    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    // Prompts user to grant or deny the ACCESS_FINE_LOCATION permission.
    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
        )
    }

    // Gets user's current location and moves map to center on location.
    private fun getCurrentLocation() {
        // 1 - Checks if permission was granted before requesting location.
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            // 2 - If permission wasn't granted, then requestLocationPermissions() is called.
            requestLocationPermissions()
        }   else {
            // Displays the blue dot of user's location.
            map.isMyLocationEnabled = true

            // 3 - lastLocation is actually a Task that runs in the background to fetch location.
            //  Requesting to be notified when location is ready by adding OnCompleteListener.
            fusedLocationClient.lastLocation.addOnCompleteListener {
                // it represents Location object with last known location.
                val location = it.result
                if (location != null) {
                    // 4 - If location is not null, creates LatLng obj from location.
                    val latLng = LatLng(location.latitude, location.longitude)

                    // 6 - Creates a CameraUpdate obj, which specifies how map camera is updated.
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    // 7  - Calls  moveCamera() to update with CameraUpdate obj.
                    map.moveCamera(update)
                }   else {
                    // 8 - If result is null, logs an error message.
                    Log.e(TAG, "No location found")
                }
            }
        }
    }

    private fun setupMapListeners() {
        map.setInfoWindowAdapter(BookmarkInfoWindowAdapter(this))
        map.setOnPoiClickListener {
            displayPoi(it)
        }
        map.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }

        fab.setOnClickListener {
            searchAtCurrentLocation()
        }

        map.setOnMapLongClickListener { latLng ->
            newBookmark(latLng)
        }
    }

    // References the refactored method to get details of POIs.
    private fun displayPoi(pointOfInterest: PointOfInterest) {
        // Displays the progress bar after searching for a place.
        showProgress()
        displayPoiGetPlaceStep(pointOfInterest)
    }

    // Refactored code - Gives details for the POIs.
    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        // 1 - Retrieves the placeId that identifies POI.
        val placeId = pointOfInterest.placeId

        // 2 - Creates field mask that ensures only requested data is retrieved.
        val placeFields = listOf(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.PHONE_NUMBER,
                Place.Field.PHOTO_METADATAS,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.TYPES)

        // 3 - Two objects to create fetch request.
        val request = FetchPlaceRequest
                .builder(placeId, placeFields)
                .build()

        // 4 - placesClient handles the request to get the place details.
        placesClient.fetchPlace(request)
                // Checks if response is successfully retrieved.
                .addOnSuccessListener { response ->
                    // 5 - Retrieves place objects, which has the details.
                    val place = response.place
                    // Displays the details on the screen.
                    displayPoiGetPhotoStep(place)
                    // Adds failure listener to catch any exceptions if request fails.
                }.addOnFailureListener { exception ->
                    // 6 - Checks if API error occurred.
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        // Logs status code and message to use for debugging.
                        Log.e(
                            TAG,
                                "Place not found: " +
                                        exception.message + ", " +
                                        "statusCode: " + statusCode)
                        // Hides progress bar if place can't be retreived.
                        hideProgress()
                    }
                }
    }

    // Passes a max width and height to get scaled-down version of original photo.
    private fun displayPoiGetPhotoStep(place: Place) {
        // 1 - Gets the first and only PhotoMetaData obj from retrieved array.
        val photoMetadata = place
                .photoMetadatas?.get(0)
        // 2 - If no photo for place, skips to the next step.
        if (photoMetadata == null) {
            // Passes along object and a null bitmap image.
            displayPoiDisplayStep(place, null)      // Next step here
            return
        }
        // 3 - Uses builder pattern to create FetchPhotoRequest.
        val photoRequest = FetchPhotoRequest
                // Passes builder the photoMetaData, a maxwidth and maxheight for retrieved photo.
                .builder(photoMetadata)
                .setMaxWidth(resources.getDimensionPixelSize(
                    R.dimen.default_image_width
                ))
                .setMaxHeight(resources.getDimensionPixelSize(
                    R.dimen.default_image_height
                ))
                .build()
        // 4 - Calls fetchPhoto passing in photoRequest; lets callbacks handle response.
        placesClient.fetchPhoto(photoRequest)
                // If response is successful, assign photo to bitmap.
                .addOnSuccessListener { fetchPhotoResponse ->
                    val bitmap = fetchPhotoResponse.bitmap
                    // Passes along place obj and the bitmap image bitmap.
                    displayPoiDisplayStep(place, bitmap)    // Next step here
                }.addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        // If unsuccessful, logs an error.
                        val statusCode = exception.statusCode
                        Log.e(
                            TAG,
                            "Place not found: " +
                            exception.message + ", " +
                            "statusCode: " + statusCode)

                        // Hides progress bar if error fetching photo.
                        hideProgress()
                    }
                }
    }

    // Displays a marker with place details and photo.
    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        // Hides progress bar before new marker is shown.
        hideProgress()

        // Adds a marker to the map by creating newMarkerOptions obj.
        val marker = map.addMarker(MarkerOptions()
            // Sets properties to place details and iconPhoto.
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber))

        // Marker tag holds full place obj and associated bitmap photo.
        marker?.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
        // Instructs map to display the Info window for the marker.
        marker?.showInfoWindow()
    }

    // Handles taps on a place info window.
    // Saves the bookmark if it hasn't been saved before,
    // or it starts the bookmark details Activity
    // if the bookmark hasn't already been saved.
    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag) {
            is MapsActivity.PlaceInfo -> {
                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null && placeInfo.image != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(
                            placeInfo.place,
                            placeInfo.image)
                    }
                }
                marker.remove();
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkMarkerView = (marker.tag as
                        MapsViewModel.BookmarkView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    // Helper method that adds a single blue marker to the map based on BookmarkMarkerView.
    private fun addPlaceMarker(
        bookmark: MapsViewModel.BookmarkView): Marker? {

        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .title(bookmark.name)
            .snippet(bookmark.phone)
            .icon(bookmark.categoryResourceId?.let {
                BitmapDescriptorFactory.fromResource(it)
            })
            .alpha(0.8f))

        marker.tag = bookmark

        // Adds a new entry to markers when a new marker is added to the map.
        bookmark.id?.let { markers.put(it, marker) }

        return marker
    }

    // Walks through a list of BookmarkMarkerView objects
    // and calls addPlaceMarker() for each.
    private fun displayAllBookmarks(
        bookmarks: List<MapsViewModel.BookmarkView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun createBookmarkObserver() {
        // 1 - Uses getBookmarkMarkerViews() on MapsViewModel to retrieve a LiveData object.
        mapsViewModel.getBookmarkViews()?.observe(
            // Telling the observer to follow lifecycle of current activity.
            this, Observer<List<MapsViewModel.BookmarkView>> {
                // 2 - When has updated data, clears all existing markers on the map.
                map.clear()
                markers.clear()
                // 3 - Calls displayAllBookmarks() passing in list of updated objects.
                it?.let {
                    displayAllBookmarks(it)
                    // Sets the new list of BookmarkView items on the recycler view
                    // adapter whenever bookmark data changes.
                    bookmarkListAdapter.setBookmarkData(it)
                }
            })
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        // Adds the bookmarkID as an extra parameter on the Intent.
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    // Activates support for the support toolbar in the maps Activity.
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        // ActionBarDrawerToggle takes drawerLayout and toolbar
        // and fully manages the display and functionality of the toggle icon.
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer)
        toggle.syncState()
    }

    // Sets up adapter for the bookmark recycler view.
    private fun setupNavigationDrawer() {
        // Gets RecyclerView from the Layout,
        // sets a default LinearLayoutManager for the RecyclerView.
        val layoutManager = LinearLayoutManager(this)
        bookmarkRecyclerView.layoutManager = layoutManager
        // Then creates a new BookmarkListAdapter
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        // and assigns it to the RecyclerView.
        bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    // Pans and zooms the map to center over a Location.
    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 16.0f))
    }

    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView) {
        // 1 - Before zooming the bookmark, the nav drawer is closed.
        drawerLayout.closeDrawer(drawerView)
        // 2 - The markers HashMap is used to look up the Marker.
        val marker = markers[bookmark.id]
        // 3 - If marker is found, its Info window is shown.
        marker?.showInfoWindow()
        // 4 - A Location obj is created from the bookmark,
        // and updateMapToLocation() is called to zoom the map to bookmark.
        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun searchAtCurrentLocation() {
        // 1 - Defines fields, which informs the Autocomplete widget what
        // attributes to return for each place.
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.TYPES)

        // 2 - Computes the bounds of the currently visible region of the map.
        val bounds = RectangularBounds.newInstance(
            map.projection.visibleRegion.latLngBounds)
        try {
            // 3 - AutoComplete provides an IntentBuilder method to build up Intent.
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, placeFields)
                .setLocationBias(bounds)
                .build(this)
            // 4 - Starts the Activity and passes
            // request code of AUTOCOMPLETE_REQUEST_CODE.
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }   catch (e: GooglePlayServicesRepairableException) {
            //TODO: Handle exception
        }   catch (e: GooglePlayServicesNotAvailableException) {
            // TODO: Handle exception
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 1 - Checks requestCode to make sure it matches the
        // AUTOCOMPLETE_REQUEST_CODE passed into startActivityForResult().
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                // 2 - If resultCode indicates user found a place and data is
                // not null, continues to process the results.
                if(resultCode == Activity.RESULT_OK && data != null) {
                    // 3  - Takes data and returns a populated Place object.
                    val place = Autocomplete.getPlaceFromIntent(data)
                    // 4 - Converts place latLng to a location and passes that to
                    // the existing updateMapToLocation method.
                    // Map will zoom to the place.
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?: 0.0
                    location.longitude = place.latLng?.longitude ?: 0.0
                    updateMapToLocation(location)
                    // Displays progress bar after searching for a place.
                    showProgress()
                    // 5 - Loads the place photo and displays the place info window.
                    displayPoiGetPhotoStep(place)
                }
        }
    }

    // Creates a new bookmark from a location and start the bookmark details
    // Activity to allow editing of the new bookmark.
    private fun newBookmark(latLng: LatLng) {
        GlobalScope.launch {
            val bookmarkId = mapsViewModel.addBookmark(latLng)
            bookmarkId?.let {
                startBookmarkDetails(it)
            }
        }
    }

    // Sets a flag on main window to prevent user touches.
    private fun disableUserInteraction() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    // Clears the flag set by disableUserInteraction().
    private fun enableUserInteraction() {
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    // Makes progress bar visible and disables user interaction.
    private fun showProgress() {
        progressBar.visibility = ProgressBar.VISIBLE
        disableUserInteraction()
    }

    // Hides the progress bar and enables user interaction.
    private fun hideProgress() {
        progressBar.visibility = ProgressBar.GONE
        enableUserInteraction()
    }

    companion object {
        // Defines a key for storing the bookmark ID in the intent extras.
        const val EXTRA_BOOKMARK_ID =
            "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        private const val  AUTOCOMPLETE_REQUEST_CODE = 2
    }


    // Class to hold original Place obj and image.
    class PlaceInfo(val place: Place? = null,
       val image: Bitmap? = null)
}