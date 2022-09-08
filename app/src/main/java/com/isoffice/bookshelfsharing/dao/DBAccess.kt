package com.isoffice.bookshelfsharing.dao

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DBAccess {
    val database: DatabaseReference = Firebase.database.reference
}
