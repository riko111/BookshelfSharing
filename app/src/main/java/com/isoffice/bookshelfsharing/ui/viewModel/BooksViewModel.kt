package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.BookInfo

data class BooksListState(
    val bookList: MutableList<BookInfo> = mutableStateListOf(),
    val tagSet: MutableSet<String> = mutableSetOf()
)

class BooksViewModel (private val bookDao: BookDao):ViewModel() {

    var booksState = BooksListState()
        private set


    fun getAllBooksList(){  // deleteFlagがFalseのものを全件取得
        val list = bookDao.readAllBooks()
        val tagSet = getAllTags(list)
        booksState = BooksListState(list, tagSet)
    }


    fun deleteBook(key:String){    //keyで論理削除
        val list = bookDao.deleteBook(key)
        val tagSet = getAllTags(list)
        booksState = BooksListState(list,tagSet)
    }

    private fun getAllTags(list:MutableList<BookInfo>): MutableSet<String> {
        val tagSet = mutableSetOf<String>()

        list.forEach{ bookInfo ->
            val book = bookInfo.book
            tagSet.addAll(book.tags.filterNot { it.isBlank() }.toSet())
        }
        return tagSet
    }


}
