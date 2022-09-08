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
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.Value
import timber.log.Timber


class BookDao(private val database: DatabaseReference) {
    val bookList = mutableStateListOf<Book>()
    var book = mutableStateOf<Book?>(null)
    fun writeNewBook(book:Book) {
        val key = database.push().key
        database.child("books").child(key!!).setValue(book)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
            }
    }

    fun searchBookList(book: Book): Boolean{
        var findFlag = false
        val bookList = readBookList()
        for(b in bookList){
            if(b.isbn == book.isbn){
                findFlag = true
                break
            }
        }
        return findFlag

    }

    fun readBookList():SnapshotStateList<Book> {
        var childrenNode: MutableIterable<DataSnapshot>?
        database.child("books").addValueEventListener(object :ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                childrenNode = dataSnapshot.children

                if(childrenNode != null) {
                    bookList.removeAll(bookList)
                    for (child in childrenNode!!) {
                        val key = child.key
                        val isbn = child.child("isbn").value.toString()
                        val title = child.child("title").value.toString()
                        val subtitle = child.child("subtitle").value?.toString()
                        val series = child.child("series").value?.toString()
                        val author = child.child("author").value.toString()
                        val thumbnail = child.child("thumbnail").value?.toString()
                        val ownerId = child.child("ownerId").value.toString()
                        val publishedDate = child.child("publishedDate").value?.toString()
                        val publisher = child.child("publisher").value?.toString()
                        val description = dataSnapshot.child("description").value?.toString()
                        val ownerIcon = child.child("ownerIcon").value?.toString()
                        val deleteFlag = child.child("deleteFlag").value?.toString().toBoolean()

                        if(!deleteFlag) {
                            val book = Book(
                                isbn = isbn,
                                title = title,
                                subtitle = subtitle,
                                series = series,
                                author = author,
                                thumbnail = thumbnail,
                                publishedDate = publishedDate,
                                publisher = publisher,
                                ownerId = ownerId,
                                ownerIcon = ownerIcon,
                                description = description,
                                key = key,
                                deleteFlag = deleteFlag,
                            )
                            bookList.add(book)
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Timber.w( "loadPost:onCancelled${databaseError.toException()}")
            }
        })
        return bookList
    }

    fun readBook(key:String): MutableState<Book?> {
        database.child("books").child(key).addValueEventListener(object :ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val isbn = dataSnapshot.child("isbn").value.toString()
                val title = dataSnapshot.child("title").value.toString()
                val subtitle = dataSnapshot.child("subtitle").value?.toString()
                val series = dataSnapshot.child("series").value?.toString()
                val author = dataSnapshot.child("author").value.toString()
                val thumbnail = dataSnapshot.child("thumbnail").value?.toString()
                val ownerId = dataSnapshot.child("ownerId").value.toString()
                val publishedDate = dataSnapshot.child("publishedDate").value?.toString()
                val publisher = dataSnapshot.child("publisher").value?.toString()
                val description = dataSnapshot.child("description").value?.toString()
                val ownerIcon = dataSnapshot.child("ownerIcon").value?.toString()
                //val purchaseDate = child.child("purchaseDate").value?.toString()
                book = mutableStateOf(Book(
                    isbn = isbn,
                    title = title,
                    subtitle = subtitle,
                    series = series,
                    author = author,
                    thumbnail = thumbnail,
                    publishedDate = publishedDate,
                    publisher = publisher,
                    ownerId = ownerId,
                    description = description,
                    ownerIcon = ownerIcon,
                    key = key,
                ))
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return book
    }

    fun deleteBook(key:String){
        val updates = HashMap<String, Any>()
        updates["/books/$key/deleteFlag"] = true
        database.updateChildren(updates)
    }
}

