package com.isoffice.bookshelfsharing.dao

import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookDataSnapshot
import com.isoffice.bookshelfsharing.model.OpenBD

object BookOpenBDMapper {
    fun openBDToBook(item: OpenBD, user: FirebaseUser) =
        item.run {
            var description : String? = null
            for(str in this.onix.collateralDetail.textContent){
                if(str.textType == "03"){
                    description = str.text
                    break
                }
            }

            Book(
                summary.isbn,
                summary.title,
                onix.descriptiveDetail.titleDetail.titleElement.subtitle?.content,
                summary.series,
                summary.author,
                summary.publisher,
                summary.pubdate,
                summary.cover,
                user.email.toString(),
                user.photoUrl.toString(),
                description = description,
                key = null,
                deleteFlag = false,
            )
        }
}

