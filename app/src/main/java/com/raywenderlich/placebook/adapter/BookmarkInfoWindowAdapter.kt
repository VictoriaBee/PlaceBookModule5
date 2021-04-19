package com.raywenderlich.placebook.adapter

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel

// 1 - A single parameter for hosting activity;
//      implements GoogleMap.InfoWindowAdapter interface.
class BookmarkInfoWindowAdapter(val context: Activity) :
    GoogleMap.InfoWindowAdapter {

    // 2 - Declaring property contents to hold contents view.
    private val contents: View = context.layoutInflater.inflate(
        R.layout.content_bookmark_info, null)

    // 4 - Overrides getInfoWindow() and returns null to show won't be
    //      replacing entire info window.
    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

    // 5 - Overrides getInfoWindow() and fills in the titleView
    //      and phoneView widgets on the Layout.
    override fun getInfoContents(marker: Marker?): View {
        val titleView = contents.findViewById<TextView>(R.id.title)
        titleView.text = marker?.title ?: ""

        val phoneView = contents.findViewById<TextView>(R.id.phone)
        phoneView.text = marker?.snippet ?: ""

        // To make imageView work.
        val imageView = contents.findViewById<ImageView>(R.id.photo)

        when (marker?.tag) {
            // 1 - If marker.tag is a MapsActivity.PlaceInfo, sets the
            // imageView bitmap directly from PlaceInfo.image object.
            is MapsActivity.PlaceInfo -> {
                imageView.setImageBitmap((marker.tag as MapsActivity.PlaceInfo).image)
            }
            // 2 - If marker.tag is MapsViewModel.BookmarkMarkerView, sets the
            // imageView bitmap from the BookmarkMarkerView.
            is MapsViewModel.BookmarkView -> {
                var bookMarkview = marker.tag as
                        MapsViewModel.BookmarkView
                // Set imageView bitmap here
                imageView.setImageBitmap(bookMarkview.getImage(context))
            }
        }

        return contents
    }
}