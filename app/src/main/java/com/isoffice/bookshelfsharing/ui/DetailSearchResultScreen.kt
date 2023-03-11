package com.isoffice.bookshelfsharing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import com.isoffice.bookshelfsharing.ui.localData.getBooksListByDetailSearch
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel

@Composable
fun DetailSearchResultScreen(
    navController: NavHostController,
    booksViewModel: BooksViewModel,
    scrollViewModel: ScrollViewModel,
    searchStr: String){

    booksViewModel.getAllBooksList()
    val allList = booksViewModel.booksState.bookList

    val searchMap = mutableMapOf<String, String>()
    searchStr.split(",").forEach(){
        val tmp = it.split("=")
        searchMap[tmp[0]] = tmp[1]
    }

    val resultList = getBooksListByDetailSearch(searchMap, allList)

    SearchResultCompose(
        resultList,
        "詳細検索",
        { navController.navigate("main")},
        { navController.navigate("bookDetail/$it")},
        { booksViewModel.deleteBook(it) },
        { scrollViewModel.setScrollIndex(it)},
        scrollViewModel.state.collectAsState().value.scrollIndex
    )

}