package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.isoffice.bookshelfsharing.ui.viewModel.BookListState
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel


@Composable
fun TitleSearchResultScreen(
    navController:NavHostController,
    booksViewModel: BooksViewModel,
    scrollViewModel: ScrollViewModel,
    title: String
) {
    booksViewModel.searchTitle(title)
    val state by booksViewModel.state.collectAsState()

    TitleSearchResultScreen(
        state,
        title,
        { navController.navigate("main")},
        { navController.navigate("bookDetail/$it")},
        { booksViewModel.deleteBook(it) },
        { scrollViewModel.setScrollIndex(it)},
        scrollViewModel.state.collectAsState().value.scrollIndex
    )

}

@Composable
private fun TitleSearchResultScreen(
    state: BookListState,
    title: String,
    onNavigateToMain:()->Unit,
    onNavigateToDetail:(str:String)->Unit,
    onClickDelete:(key:String)->Unit,
    setScrollIndex:(index:Int)->Unit,
    index:Int
    ){
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppBar(title, onNavigateToMain)
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            if (state.bookList == null) {
                CircleProgressIndicator()
            } else if (state.bookList.isEmpty()) {
                Column {}
            } else {
                MainContent(
                    state.bookList,
                    onNavigateToDetail,
                    onClickDelete,
                    setScrollIndex,
                    index
                )
            }
        }
    }
}

@Composable
private fun AppBar(title: String, onNavigateToMain:()->Unit){
    TopAppBar(
        title = { Text(text = "「$title」で始まるタイトル") },
        navigationIcon ={
            IconButton(onClick = { onNavigateToMain() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "" )
            }
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}