package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.isoffice.bookshelfsharing.ui.viewModel.BooksListState
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel

@Composable
fun TagSearchResultScreen(
    navController: NavHostController,
    booksViewModel: BooksViewModel,
    scrollViewModel: ScrollViewModel,
    tag:String
) {
    val state = booksViewModel.booksState
    booksViewModel.searchTag(tag)

    TagSearchResultScreen(
        state,
        tag,
        { navController.navigate("main")},
        { navController.navigate("bookDetail/$it")},
        { booksViewModel.deleteBook(it) },
        { scrollViewModel.setScrollIndex(it)},
        scrollViewModel.state.collectAsState().value.scrollIndex
    )
}
@Composable
private fun TagSearchResultScreen(
    bookState: BooksListState,
    tag: String,
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
            AppBar(tag, onNavigateToMain)
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            if (bookState.bookList.isEmpty()) {
                CircleProgressIndicator()
            } else {
                MainContent(
                    bookState.bookList,
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
private fun AppBar(tag: String, onNavigateToMain:()->Unit){
    TopAppBar(
        title = { Text(text = "タグ「$tag」") },
        navigationIcon ={
            IconButton(onClick = { onNavigateToMain() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "" )
            }
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}