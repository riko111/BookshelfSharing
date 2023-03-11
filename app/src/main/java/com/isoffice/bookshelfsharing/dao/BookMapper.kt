package com.isoffice.bookshelfsharing.dao

import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookDataSnapshot
import com.isoffice.bookshelfsharing.model.GoogleBooks
import com.isoffice.bookshelfsharing.model.OpenBD

object BookOpenBDMapper {
    fun openBDToBook(item: OpenBD, user: FirebaseUser) =
        item.run {
            var furigana = ""
            var description : String? = null
            if(this.onix.descriptiveDetail.titleDetail.titleElement.titleText != null){
                furigana = this.onix.descriptiveDetail.titleDetail.titleElement.titleText.collationkey
            }

            val descriptionList = mutableListOf<String>()
            for(str in this.onix.collateralDetail.textContent){
                descriptionList.add(str.text)
            }

            if(descriptionList.isNotEmpty()) {
                var min = 0
                for (index in 1 until descriptionList.size) {
                    if (descriptionList[index].length < descriptionList[min].length) {
                        min = index
                    }
                }
                description = descriptionList[min]
            }

            Book(
                summary.isbn,
                summary.title,
                furigana,
                onix.descriptiveDetail.titleDetail.titleElement.subtitle?.content,
                summary.series,
                summary.author,
                summary.publisher,
                summary.pubdate,
                summary.cover,
                user.email.toString(),
                user.photoUrl.toString(),
                description = description,
                deleteFlag = false,
            )
        }
}

object GoogleBooksMapper {
    fun GooglsBookToBook(item: GoogleBooks, user: FirebaseUser) =
        item.run {
            val info = item.items!![0].volumeInfo
            val furigana = ""
            var isbn : String? = null
            if(info.industryIdentifiers != null){
                info.industryIdentifiers.forEach(){
                    if(it.type == "ISBN_13"){
                        isbn = it.identifier
                        return@forEach
                    }
                }
            }
            var cover : String? = null
            if(info.imageLinks?.thumbnail != null){
                cover = info.imageLinks.thumbnail
            }

            Book(
                isbn,
                info.title,
                furigana,
                info.subtitle,
                "",
                info.authors?.joinToString()?:"",
                info.publisher,
                info.publishedDate,
                cover,
                user.email.toString(),
                user.photoUrl.toString(),
                description = info.description,
                deleteFlag = false,
            )
        }
}

