package com.isoffice.bookshelfsharing.model

import com.google.firebase.database.Exclude
import java.util.*

data class Login (var id : String, var login:  String){
    @Exclude
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "id" to id,
            "login" to login
        )
    }
}