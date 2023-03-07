package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.BookInfo

data class BooksListState(
    val bookList: MutableList<BookInfo> = mutableStateListOf()
)

class BooksViewModel (private val bookDao: BookDao):ViewModel() {

    var booksState = BooksListState()
        private set


    fun getAllBooksList(){  // deleteFlagがFalseのものを全件取得
        val list = bookDao.readAllBooks()
        booksState = BooksListState(list)
    }


    fun deleteBook(key:String){    //keyで論理削除
        val list = bookDao.deleteBook(key)
        booksState = BooksListState(list)
    }
/*
    fun searchTitle(title:String) {
        val list = bookDao.titleSearchBook(title)
        booksState = BooksListState(list)
    }
*/


    fun searchTag(tag:String) {
        val list = bookDao.searchTag(tag)
        booksState = BooksListState(list)
    }
}


/*data class BookListState(
    val bookList: MutableList<BookInfo>?
)
{
    companion object{
        val initList = BookListState(null)
    }
}*/
