package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        val oldState = currentState()
        val list = bookDao.titleSearchBook(title)
        updateState { oldState.copy(bookList = MutableStateFlow(list).value) }

    }
}


data class BookListState(
    val bookList: SnapshotStateList<Book>?
)
{
    companion object{
        val initList = BookListState(null)
    }
}
