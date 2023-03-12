package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TagSetState(
    var tagSetOnScreen: MutableSet<String> = mutableSetOf()
)

class TagsViewModel(private val bookDao: BookDao): ViewModel() {
    var state = TagSetState()
        private set

    fun setTagSet(tagSet:MutableSet<String>?){
        state = tagSet?.let { TagSetState(it) }!!
    }

    fun addTagSet(key:String, tag:String){
        bookDao.addTagSet(key, tag)
        val oldSet = state.tagSetOnScreen
        oldSet.add(tag)
        state = TagSetState(oldSet)
    }

    fun deleteTag(key: String, tag:String) {
        bookDao.deleteTag(key,tag)
        val oldSet = state.tagSetOnScreen
        oldSet.remove(tag)
        state = TagSetState(oldSet)
    }


}

