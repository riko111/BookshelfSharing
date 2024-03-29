package com.isoffice.bookshelfsharing.model

data class Book(
    val isbn : String? = null,
    val title: String,
    val furigana: String = "",
    val subtitle: String? = null,
    val series : String? = null,
    val author: String? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val thumbnail: String? = null,
    val ownerId : String? = null,
    val ownerIcon : String? = null,
    val description: String? = null,
    val tags: MutableSet<String>? = null,
    var deleteFlag : Boolean = false,
)

data class BookInfo(
    val key : String,
    val book: Book
)