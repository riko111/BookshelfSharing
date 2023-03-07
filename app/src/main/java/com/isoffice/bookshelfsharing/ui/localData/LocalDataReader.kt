package com.isoffice.bookshelfsharing.ui.localData

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberUpdatedState
import com.isoffice.bookshelfsharing.model.BookInfo
import com.isoffice.bookshelfsharing.ui.viewModel.BooksListState


fun getUserBooksList(user:String, allList:MutableList<BookInfo>,deleteFlag:Boolean):MutableList<BookInfo>{
    val list = mutableStateListOf<BookInfo>()
    allList.forEach {
        if(it.book.ownerId == user){
            if(!deleteFlag && it.book.deleteFlag) return@forEach
            list.add(it)
        }
    }
    return list
}
fun getOtherBooksList(user:String, allList:MutableList<BookInfo>,deleteFlag:Boolean):MutableList<BookInfo>{
    val list = mutableStateListOf<BookInfo>()
    allList.forEach {
        if(it.book.ownerId != user){
            if(!deleteFlag && it.book.deleteFlag) return@forEach
            list.add(it)
        }
    }
    return list
}

fun getBooksListByTitle(key:String, allList:MutableList<BookInfo>):MutableList<BookInfo> {
    val list = mutableStateListOf<BookInfo>()
    allList.forEach {
        if(it.book.title.contains(key)){
            list.add(it)
        }
    }
    return list
}

fun getBooksListByTag(key:String, allList:MutableList<BookInfo>):MutableList<BookInfo> {
    val list = mutableStateListOf<BookInfo>()
    allList.forEach {
        if(it.book.tags.contains(key)){
            list.add(it)
        }
    }
    return list
}

fun getBooksListByDetailSearch(map:MutableMap<String, String>, allList: MutableList<BookInfo>):MutableList<BookInfo> {
    val list = mutableStateListOf<BookInfo>()
    allList.forEach{
        val book = it.book
        if(map.contains("title")){
            if(!book.title.contains(map["title"].toString())) return@forEach
        }
        if(map.contains("author")){
            if(!book.author.isNullOrEmpty() && !book.author.contains(map["author"].toString())) return@forEach
        }
        if(map.contains("publisher")){
            if(!book.publisher.isNullOrEmpty() &&  !book.publisher.contains(map["publisher"].toString())) return@forEach
        }
        if(map.contains("tag")){
            if(!book.tags.contains(map["tag"].toString())) return@forEach
        }
        list.add(it)
    }

    return list
}
