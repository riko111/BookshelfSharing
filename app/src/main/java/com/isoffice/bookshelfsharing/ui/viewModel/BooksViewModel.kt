package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.BookInfo

data class BooksListState(
    val bookList: List<BookInfo> = emptyList(),
    val tagSet: Set<String> = emptySet()
)

class BooksViewModel(private val bookDao: BookDao) : ViewModel() {

    var booksState by mutableStateOf(BooksListState())
        private set

    fun getAllBooksList() {
        bookDao.readAllBooks { updatedList ->
            updateBooksState(updatedList)
        }
    }

    fun deleteBook(key: String) {
        bookDao.deleteBook(key)
        // BookDao内のSnapshotListenerがリストを更新するため、
        // getAllBooksList()を呼んでStateを最新の状態（タグの再計算など）に更新します。
        getAllBooksList()
    }

    private fun getAllTags(list: MutableList<BookInfo>): Set<String> {
        val tagSet = mutableSetOf<String>()
        list.forEach { bookInfo ->
            bookInfo.book.tags?.let { tags ->
                tagSet.addAll(tags.filterNot { it.isBlank() })
            }
        }
        return tagSet
    }

    private fun updateBooksState(list: List<BookInfo>) {
        val mutableList = list.toMutableList()
        val tagSet = getAllTags(mutableList)
        booksState = BooksListState(
            bookList = mutableList,
            tagSet = tagSet
        )
    }
}
