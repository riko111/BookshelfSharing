package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait

class BookViewModel(private val bookDao: BookDao): ViewModel() {
    private val booksStateFlow = MutableStateFlow(BookState.init)
    val state = booksStateFlow.asStateFlow()

    private fun currentState() = booksStateFlow.value
    private fun updateState(newState: () -> BookState){
        booksStateFlow.value = newState()
    }

    fun getBookByKey(key:String){
        val oldState = currentState()
        val book = bookDao.readBook(key)
        updateState { oldState.copy(book = book) }
    }

    fun getBookByIsbn(isbn:String){
        val oldState = currentState()
        val book = bookDao.searchIsbnBook(isbn)
        updateState { oldState.copy(book = book) }
    }

    fun addBook(book: Book){
        bookDao.writeNewBook(book)
    }

    fun updateBook(key:String,book:Book){
        bookDao.updateBook(key,book)
    }

    fun addTag(key:String,tag:String){
        val oldState = currentState()
        bookDao.addTagSet(key,tag)
        val book = currentState().book
        if (book != null) {
            book.value!!.book.tags?.add(tag)
        }
        updateState {  oldState.copy(book = book) }
    }

    fun deleteTag(key:String,tag:String){
        val oldState = currentState()
        bookDao.deleteTag(key,tag)
        val book = currentState().book
        if (book != null) {
            book.value!!.book.tags?.remove(tag)
        }
        updateState {  oldState.copy(book = book) }
    }

}

data class BookState(
    var flag : Boolean,
    var book:  MutableState<BookInfo?>?
)
{
    companion object{
        val init = BookState(false,null)
    }
}
