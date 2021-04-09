package com.raywenderlich.placebook.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookmarkInfoWindowAdapter
import com.raywenderlich.placebook.viewmodel.MapsViewModel

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Private members.
    private lateinit var map: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
    }

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
    }
    // Creating the PlacesClient.
    private fun setupPlacesClient() {
        Places.initialize(getApplicationContext(),       // Changed syntax.
            getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
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
    }

    // References the refactored method to get details of POIs.
    private fun displayPoi(pointOfInterest: PointOfInterest) {
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
                Place.Field.LAT_LNG)

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
                    }
                }
    }

    // Passes a max width and height to get scaled-down version of original photo.
    private fun displayPoiGetPhotoStep(place: Place) {
        // 1 - Gets the first and only PhotoMetaData obj from retrieved array.
        val photoMetadata = place
                .getPhotoMetadatas()?.get(0)
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
                    }
                }
    }

    // Displays a marker with place details and photo.
    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        // Adds a marker to the map by creating newMarkerOptions obj.
        map.addMarker(MarkerOptions()
            // Sets properties to place details and iconPhoto.
            .position(place.latLng as LatLng)
            .title(place.name)
            .snippet(place.phoneNumber))

        // Marker tag holds full place obj and associated bitmap photo.
        marker?.tag = PlaceInfo(place, photo)
    }

    // Handles taps on a pace info window.
    private fun handleInfoWindowClick(marker: Marker) {
        val placeInfo = (marker.tag as PlaceInfo)
        GlobalScope.launch {
            mapsViewModel.addBookmarkFromPlace(placeInfo.place,
                placeInfo.image)
        }
        marker.remove()
    }

    // Helper method that adds a single blue marker to the map based on BookmarkMarkerView.
    private fun addPlaceMarker(
        bookmark: MapsViewModel.BookMarkerView): Marker? {

        val marker = map.addMarker(MarkerOptions()
            .position(bookmark.location)
            .icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_AZURE))
            .alpha(0.8f))

        marker.tag = bookmark

        return marker
    }

    // Walks through a list of BookmarkMarkerView objects
    // and calls addPlaceMarker() for each.
    private fun displayAllBookmarks(
        bookmarks: List<MapsViewModel.BookMarkerView>) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    // Class to hold original Place obj and image.
    class PlaceInfo(val place: Place? = null,
       val image: Bitmap? = null)
}