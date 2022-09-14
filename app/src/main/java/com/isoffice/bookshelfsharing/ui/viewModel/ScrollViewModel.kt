package com.isoffice.bookshelfsharing.ui.viewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScrollViewModel: ViewModel() {
    private val scrollStateFlow = MutableStateFlow(ScrollState.initIndex)
    val state = scrollStateFlow.asStateFlow()

    private fun currentState() = scrollStateFlow.value
    private fun updateState(newState: ()->ScrollState){
        scrollStateFlow.value = newState()
    }

    fun setScrollIndex(index:Int){
        val oldState = currentState()
        updateState { oldState.copy(scrollIndex = index) }
    }
}

data class ScrollState(
    val scrollIndex : Int
){
    companion object{
        val initIndex = ScrollState(0)
    }
}