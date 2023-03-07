package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.isoffice.bookshelfsharing.model.BookInfo


@Composable
fun SearchResultContents(
    bookList: MutableList<BookInfo>,
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
            if (bookList.isEmpty()) {
                //CircleProgressIndicator()
                Text("該当する書籍がありません")
            } else {
                MainContent(
                    bookList,
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
        title = { Text(text = title) },
        navigationIcon ={
            IconButton(onClick = { onNavigateToMain() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "" )
            }
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}