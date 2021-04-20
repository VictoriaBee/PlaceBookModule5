package com.raywenderlich.placebook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.ui.MapsActivity
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.android.synthetic.main.bookmark_item.view.*

// 1 - Adapter constructor takes two arguments.
class BookmarkListAdapter(
    private var bookmarkData: List<MapsViewModel.BookmarkView>?,
    private val mapsActivity: MapsActivity) :
    RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>() {

    // 2 - Defined to hold the view widgets.
        class ViewHolder(v: View,
        private val mapsActivity: MapsActivity) :
                RecyclerView.ViewHolder(v) {
                // Called when ViewHolder is initialized.
                init {
                    v.setOnClickListener {
                        val bookmarkView = itemView.tag as MapsViewModel.BookmarkView
                        mapsActivity.moveToBookmark(bookmarkView)
                    }
                }
                    val nameTextView: TextView = v.bookmarkNameTextView
                    val categoryImageView: ImageView = v.bookmarkIcon
                }
    // 3 - Designed to be called when bookmark data changes.
    // Assigns bookmarks to the new BookmarkView List
    fun setBookmarkData(bookmarks: List<MapsViewModel.BookmarkView>) {
        this.bookmarkData = bookmarks
        // and refreshes RecyclerView by calling notifyDataSetChanged().
        notifyDataSetChanged()
    }

    // 4 - Overridden and used to create ViewHolder by inflating
    // the bookmark_item layout and passing in mapsActivity property.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): BookmarkListAdapter.ViewHolder {
        val vh = ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.bookmark_item, parent, false),
            mapsActivity)
        return vh
    }

    override fun onBindViewHolder(holder: ViewHolder,
        position: Int) {
        // 5 - Assigned to bookmarkData if not null; otherwise, returns early.
        val bookmarkData = bookmarkData ?: return
        // 6 - Assigned to the bookmark data for the current item position.
        val bookmarkViewData = bookmarkData[position]
        // 7 - Reference to the bookmarkViewData is assigned to the
        //      holder's itemView.
        holder.itemView.tag = bookmarkViewData
        holder.nameTextView.text = bookmarkViewData.name
        // Checks to see if categoryResourceId is set;
        bookmarkViewData.categoryResourceId?.let {
            // If set, sets the image resource to categoryResourceId.
            holder.categoryImageView.setImageResource(it)
        }
    }

    // 8 - Override to return the number of items in the bookmarkData list.
    override fun getItemCount(): Int {
        return bookmarkData?.size ?: 0
    }
}