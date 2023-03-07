package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.model.Book

data class TagsListState(
    val tagListOnScreen: MutableList<String> = mutableStateListOf()
)
class TagsViewModel:ViewModel() {

    var tagsState = TagsListState()
        private set

    fun setTagsList(book: Book) {
        val list = book.tags
        tagsState = TagsListState(list)
    }


}

