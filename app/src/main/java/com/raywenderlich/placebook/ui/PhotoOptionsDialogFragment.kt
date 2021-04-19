package com.raywenderlich.placebook.ui

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.DialogFragment

class PhotoOptionsDialogFragment : DialogFragment() {
    // 1 - Interface by must be implemented by the parent Activity.
    // Implemented in BookmarkDetailsActivity.
    interface PhotoOptionDialogListener {
        fun onCaptureClick()
        fun onPickClick()
    }

    // 2 - Defined to hold an instance of PhotoOptionsDialogListener.
    private lateinit var listener: PhotoOptionDialogListener
    // 3 - The standark onCreateDialog method for a DialogFragment.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 4 - Listener property set to the parent Activity.
        listener = activity as PhotoOptionDialogListener
        // 5 - Variables for initialization later.
        var captureSelectIdx = -1
        var pickSelectIdx = -1
        // 6 - Defined to hold the AlertDialog options.
        val options = ArrayList<String>()
        // 7 - Temporary un-mutable local variable to prevent compiler errors.
        val context = activity as Context
        // 8 - If device has a camera, then  Camera option is added to the array.
        if (canCapture(context)) {
            options.add("Camera")
            captureSelectIdx = 0
        }
        // 9 - If device can pick image from gallery, then Gallery option is added to array.
        if (canPick(context)) {
            options.add("Gallery")
            // Variable set to 0 if it's the first option, or to 1 if it's the second option.
            pickSelectIdx = if (captureSelectIdx == 0) 1 else 0
        }
        // 10 - Built using the options list.
        return AlertDialog.Builder(context)
            .setTitle("Photo Option")
            .setItems(options.toTypedArray<CharSequence>()) {
                _, which ->
                if (which == captureSelectIdx) {
                    // 11 - If Camera option was selected,
                    // then onCaptureClick() is called on the listener.
                    listener.onCaptureClick()
                }   else if (which == pickSelectIdx) {
                    // 12 - If Gallery was selected,
                    // then onPickClick() is called on listener.
                    listener.onPickClick()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        // 13 - Determines if device can pick image from a gallery.
        fun canPick(context: Context) : Boolean {
            // Creates an intent for picking images,
            // then checks to see if Intent can be resolved.
            val pickIntent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            return (pickIntent.resolveActivity(
                context.packageManager) != null)
        }
        // 14 - Determines if the device has a camera to capture new image.
        // Uses same technique as canPick(), but with different Intent action.
        fun canCapture(context: Context) : Boolean {
            val captureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE)
            return (captureIntent.resolveActivity(
                context.packageManager) != null)
        }
        // 15 - Helper method intended to be used by the parent activity
        //      when creating new PhotoOptionsDialogFragment
        fun newInstance(context: Context):
                PhotoOptionsDialogFragment? {
            // 16 - If device can pick from gallery or take new image,
            // then PhotoOptionDialogFragment is created and returned.
            if (canPick(context) || canCapture(context)) {
                val frag = PhotoOptionsDialogFragment()
                return frag
            } else {
                // Otherwise null is returned.
                return null
            }
        }
    }
}