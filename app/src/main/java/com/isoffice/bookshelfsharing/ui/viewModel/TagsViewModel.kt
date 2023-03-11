package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.model.Book

data class TagsListState(
    val tagSetOnScreen: MutableSet<String> = mutableSetOf()
)
class TagsViewModel:ViewModel() {

    var tagsState = TagsListState()
        private set

    fun setTagsList(set: MutableSet<String>) {
        tagsState = TagsListState(set)
    }


}

