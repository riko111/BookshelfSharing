package com.isoffice.bookshelfsharing.dao

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.Value
import timber.log.Timber


class BookDao(private val db: FirebaseFirestore) {
    private val bookList = mutableStateListOf<Book>()
    var book = mutableStateOf<Book?>(null)
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


    fun readBookList():SnapshotStateList<Book> {
        db.collection("books")
            .whereEqualTo("deleteFlag", false)
            .get()
            .addOnFailureListener {
                Timber.w("Error getting books.: $it")
            }
            .addOnSuccessListener {
                bookList.removeAll(bookList)
                for(doc in it){
                    val item = doc.data
                    val book = Book(
                        isbn = item["isbn"].toString(),
                        title = item["title"].toString(),
                        author = item["author"].toString(),
                        ownerId = item["ownerId"].toString(),
                        publisher = item["publisher"].toString(),
                        publishedDate = item["publishedDate"].toString(),
                        thumbnail = item["thumbnail"].toString(),
                    )
                    bookList.add(book)
                }
            }
        return bookList
    }

    fun readBook(isbn:String) : MutableState<Book?>{
        db.collection("books").whereEqualTo("isbn", isbn)
            .get()
            .addOnSuccessListener {
                for(doc in it){
                    val item = doc.data
                    book = mutableStateOf(Book(
                        isbn = item["isbn"].toString(),
                        title = item["title"].toString(),
                        author = item["author"]?.toString(),
                        ownerId = item["ownerId"].toString(),
                        publisher = item["publisher"]?.toString(),
                        publishedDate = item["publishedDate"]?.toString(),
                        thumbnail = item["thumbnail"]?.toString(),
                        description = item["description"]?.toString(),
                        subtitle = item["subtitle"]?.toString(),
                        series = item["series"]?.toString(),
                        ownerIcon = item["ownerIcon"].toString(),
                    ))
                }
            }
            .addOnFailureListener {
                Timber.w("Error getting books by isbn: $it")
            }
        return book
    }

    fun deleteBook(isbn:String){
        db.collection("books").whereEqualTo("isbn", isbn)
            .get()
            .addOnSuccessListener {
                val id = it.documents[0].id
                db.collection("books").document(id)
                    .update("deleteFlag", true)
            }
            .addOnFailureListener {
                Timber.w("Error delete book. :$isbn")
            }
    }

}

