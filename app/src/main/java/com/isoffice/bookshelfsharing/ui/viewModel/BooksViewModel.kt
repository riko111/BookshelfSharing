package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.BookInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class BooksViewModel (private val bookDao: BookDao):ViewModel() {
    private val booksStateFlow = MutableStateFlow(BookListState.initList)
    val state = booksStateFlow.asStateFlow()

    private fun currentState() = booksStateFlow.value
    private fun updateState(newState: () -> BookListState){
        booksStateFlow.value = newState()
    }

    fun getAllBooksList(){  // deleteFlagがFalseのものを全件取得
        val oldState = currentState()
        val list = bookDao.readAllBooks()
        updateState { oldState.copy(bookList = MutableStateFlow(list).value) }
    }

    fun deleteBook(isbn:String){    //isbnコードで削除
        val oldState = currentState()
        val list = bookDao.deleteBook(isbn)
        updateState { oldState.copy(bookList = MutableStateFlow(list).value) }
    }

    fun searchTitle(title:String) {
        Timber.d("Title:$title")
        val oldState = currentState()
        val list = bookDao.titleSearchBook(title)

        updateState { oldState.copy(bookList = MutableStateFlow(list).value) }
    }

    fun searchTag(tag:String) {
        val oldState = currentState()
        val list = bookDao.searchTag(tag)
        updateState { oldState.copy(bookList = MutableStateFlow(list).value) }
    }
}


data class BookListState(
    val bookList: MutableList<BookInfo>?
)
{
    companion object{
        val initList = BookListState(null)
    }
}
