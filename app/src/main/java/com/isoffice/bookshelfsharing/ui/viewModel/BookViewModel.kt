package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
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


    fun getBookByIsbn(isbn:String){
        val oldState = currentState()
        val flag = bookDao.searchIsbnBook(isbn)
        updateState { oldState.copy(flag = flag.value) }
    }

    fun addBook(book: Book){
        bookDao.writeNewBook(book)
    }
}

data class BookState(
    var flag : Boolean,
    var book:  MutableState<Book?>?
)
{
    companion object{
        val init = BookState(false,null)
    }
}
