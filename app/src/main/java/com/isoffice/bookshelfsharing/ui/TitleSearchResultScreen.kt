package com.isoffice.bookshelfsharing.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import com.isoffice.bookshelfsharing.ui.localData.getBooksListByTitle
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel


@Composable
fun TitleSearchResultScreen(
    navController:NavHostController,
    booksViewModel: BooksViewModel,
    scrollViewModel: ScrollViewModel,
    title: String
) {
    booksViewModel.getAllBooksList()
    val resultList = getBooksListByTitle(title, booksViewModel.booksState.bookList)

    SearchResultCompose(
        resultList,
        "「$title」を含むタイトル",
        { navController.navigate("main")},
        { navController.navigate("bookDetail/$it")},
        { booksViewModel.deleteBook(it) },
        { scrollViewModel.setScrollIndex(it)},
        scrollViewModel.state.collectAsState().value.scrollIndex
    )

}
