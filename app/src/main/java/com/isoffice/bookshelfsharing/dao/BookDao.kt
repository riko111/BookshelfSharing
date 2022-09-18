package com.isoffice.bookshelfsharing.dao

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookInfo
import timber.log.Timber


class BookDao(private val db: FirebaseFirestore) {
    var bookList = mutableListOf<BookInfo>()
    var book = mutableStateOf<BookInfo?>(null)
    var flag = mutableStateOf<Boolean>(false)
    fun writeNewBook(book:Book) {
        db.collection("books")
            .add(book)
            .addOnSuccessListener {
                Timber.d("Book added with ID: ${it.id}")
            }
            .addOnFailureListener {
                Timber.w("Error adding book: $it ")
            }
    }


    fun readAllBooks():MutableList<BookInfo> {
        db.collection("books")
            .whereEqualTo("deleteFlag", false)
            .orderBy("furigana")
            .get()
            .addOnFailureListener {
                Timber.w("Error getting books.: $it")
            }
            .addOnSuccessListener {
                bookList.removeAll(bookList)
                for(doc in it){
                    val item = doc.data
                    val book = setSnapshotToBook(item)
                    bookList.add(BookInfo(doc.id,book))
                }
                Timber.d("bookList:${bookList.size}")
                return@addOnSuccessListener
            }
        return bookList
    }

    fun readBook(key:String) : MutableState<BookInfo?>{
        db.collection("books")
            .document(key)
            .get()
            .addOnSuccessListener {
                book = mutableStateOf(BookInfo(it.id, setSnapshotToBook(it.data!!)))
            }
            .addOnFailureListener {
                Timber.w("Error getting books by isbn: $it")
            }
        return book
    }

    fun deleteBook(isbn:String):MutableList<BookInfo>{
    //fun deleteBook(isbn:String):SnapshotStateList<Book>{
            db.collection("books").whereEqualTo("isbn", isbn)
            .get()
            .addOnSuccessListener {
                if(it.documents.size > 0) {
                    val id = it.documents[0].id
                    db.collection("books").document(id)
                        .update("deleteFlag", true)
                }
                readAllBooks()
            }
            .addOnFailureListener {
                Timber.w("Error delete book. :$isbn")
            }
        return bookList
    }

    fun titleSearchBook(keyword:String):MutableList<BookInfo>{
        db.collection("books")
            .whereEqualTo("deleteFlag", false)
            .orderBy("title")
            .startAt(keyword).endAt(keyword + '\uf8ff')
            .addSnapshotListener{value,error ->
                if(error != null){
                    Timber.w("title search error.")
                } else {
                    bookList.removeAll(bookList)
                    if(value != null) {
                        for ( doc in value) {
                            val item = doc.data
                            val book = setSnapshotToBook(item)
                            bookList.add(BookInfo(doc.id, book))
                        }
                        Timber.d("titleList:${bookList.size}")
                    }
                }
            }
        return bookList
    }


    fun searchIsbnBook(isbn:String):MutableState<Boolean>{
        db.collection("books")
            .whereEqualTo("isbn", isbn)
            .get()
            .addOnFailureListener {
                Timber.w("isbn search error.")
            }
            .addOnSuccessListener {
                flag = if(it.documents.size > 0) {
                    mutableStateOf(true)
                } else {
                    mutableStateOf(false)
                }
            }

        return flag
    }

    fun addTagList(key:String, tag:String){
        val ref = db.collection("books").document(key)
        ref.update("tags", FieldValue.arrayUnion(tag))
    }

    fun deleteTag(key: String, tag: String){
        val ref = db.collection("books").document(key)
        ref.update("tags", FieldValue.arrayRemove(tag))
    }

    fun searchTag(tag: String) :MutableList<BookInfo>{
        val ref = db.collection("books")
        ref.whereArrayContains("tags", tag)
            .orderBy("furigana")
            .get()
            .addOnFailureListener {
                Timber.w("Error getting books.: $it")
            }
            .addOnSuccessListener {
                bookList.removeAll(bookList)
                for(doc in it){
                    val item = doc.data
                    val book = setSnapshotToBook(item)
                    bookList.add(BookInfo(doc.id,book))
                }
            }
        return bookList
    }

    private fun setSnapshotToBook(item: Map<String, Any>): Book {
        var list = mutableListOf("")
        if(item["tags"] !=null){
            val str = item["tags"].toString()
            list = str.removePrefix("[").removeSuffix("]").split(",").toMutableList()
        }

        return Book(
            isbn = item["isbn"].toString(),
            title = item["title"].toString(),
            furigana = item["furigana"].toString(),
            author = item["author"]?.toString(),
            ownerId = item["ownerId"].toString(),
            publisher = item["publisher"]?.toString(),
            publishedDate = item["publishedDate"]?.toString(),
            thumbnail = item["thumbnail"]?.toString(),
            description = item["description"]?.toString(),
            subtitle = item["subtitle"]?.toString(),
            series = item["series"]?.toString(),
            ownerIcon = item["ownerIcon"].toString(),
            tags = list
        )
    }
}

