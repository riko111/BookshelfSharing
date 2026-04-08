package com.isoffice.bookshelfsharing.dao

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookInfo
import timber.log.Timber


class BookDao(private val db: FirebaseFirestore) {
    private var bookList = mutableStateListOf<BookInfo>()
    private var listenerRegistration: ListenerRegistration? = null
    var book = mutableStateOf<BookInfo?>(null)
    var set = mutableSetOf<String>()
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
        if (listenerRegistration == null) {
            listenerRegistration = db.collection("books")
                .orderBy("furigana")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Timber.w("Error getting books.: $e")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        bookList.clear()
                        for (doc in snapshot) {
                            val item = doc.data
                            val book = setSnapshotToBook(item)
                            bookList.add(BookInfo(doc.id, book))
                        }
                    }
                }
        }
        return bookList
    }

    fun readBook(key:String) : MutableState<BookInfo?>{
        db.collection("books")
            .document(key)
            .get()
            .addOnSuccessListener {
                book = mutableStateOf(BookInfo(it.id, setSnapshotToBook(it.data!!)))
                return@addOnSuccessListener
            }
            .addOnFailureListener {
                Timber.w("Error getting books by isbn: $it")
            }
        return book
    }

    fun deleteBook(key:String) {
        db.collection("books")
            .document(key)
            .update("deleteFlag", true)
    }

    fun searchIsbnBook(isbn:String):MutableState<BookInfo?>{
        db.collection("books")
            .whereEqualTo("isbn", isbn)
            .get()
            .addOnFailureListener {
                Timber.w("isbn search error.")
            }
            .addOnSuccessListener {
                book = if(it.documents.isNotEmpty()) {
                    mutableStateOf(BookInfo(it.documents[0].id, setSnapshotToBook(it.documents[0].data!!)))
                } else {
                    mutableStateOf(null)
                }
            }

        return book
    }

    fun updateBook(key: String, book: Book){
        val ref = db.collection("books").document(key)
        val item = setBookToFireStore(book)
        ref.update(item)
    }

    fun addTagSet(key:String, tag:String){
        val ref = db.collection("books").document(key)
        ref.update("tags", FieldValue.arrayUnion(tag))
    }

    fun deleteTag(key: String, tag: String){
        val ref = db.collection("books").document(key)
        ref.update("tags", FieldValue.arrayRemove(tag))
    }
    private fun setTags(item: Map<String, Any>):MutableSet<String>{
        val set = mutableSetOf<String>()
        if(item["tags"] !=null){
            val str = item["tags"].toString()
            if(str.length>2) {
                set.addAll(str.removePrefix("[").removeSuffix("]").split(", ").toSet())
            }
        }
        return set
    }

    private fun setSnapshotToBook(item: Map<String, Any>): Book {
        val set = setTags(item)

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
            tags = set,
            deleteFlag = item["deleteFlag"].toString().toBoolean()
        )
    }

    private fun setBookToFireStore(book: Book):MutableMap<String, Any?> {
        val item = mutableMapOf<String, Any?>()

        item["isbn"] = book.isbn
        item["title"] = book.title
        item["furigana"] = book.furigana
        item["author"] = book.author
        item["ownerId"] = book.ownerId
        item["publisher"] = book.publisher
        item["publishedDate"] = book.publishedDate
        item["thumbnail"] = book.thumbnail
        item["description"] = book.description
        item["subtitle"] = book.subtitle
        item["series"] = book.series
        item["ownerIcon"] = book.ownerIcon
        item["tags"] = if(book.tags != null){book.tags.toString()} else {null}
        item["deleteFlag"] = book.deleteFlag.toString()

        return item
    }
}

