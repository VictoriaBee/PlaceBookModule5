package com.raywenderlich.placebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// 1 - Tells Room that this is a db entity class.
@Entity
// 2 - The primary constructor is defined using arguments for all properties
//      with default values defined.
data class Bookmark(
    // 3 - id property is defined; autoGenerate attribute tells Room to automatically
    //      generate incrementing numbers for this field.
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    // 4 - Defined values means able to construct bookmark with
    //      partial list of properties.
    var placeId: String? = null,
    var name: String = "",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var phone: String = ""
)