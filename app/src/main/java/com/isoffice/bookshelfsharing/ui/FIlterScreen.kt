package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel

@Composable
fun FilterScreen(
    navController: NavHostController,
    booksViewModel: BooksViewModel,
) {
    booksViewModel.getAllBooksList()
    val tagSet = booksViewModel.booksState.tagSet
    Scaffold(
        topBar = {TopBar{ navController.navigate("main") }}
    ){
        padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth(),
        ) {
            Main(
                tagSet,
            ) { navController.navigate("detailedSearch/$it") }
        }
    }
}

@Composable
private fun TopBar(onNavigateToMain:()->Unit) {
    TopAppBar(
        title = { Text(text = "詳細検索") },
        navigationIcon = {
            IconButton(onClick = { onNavigateToMain() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "")
            }
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
private fun Main(
    tagSet:MutableSet<String>,
    doSearch:(str:String)->Unit,
){
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("")}

    TextField(
        label = {Text(text = "タイトル")},
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        value = title, onValueChange = {title = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))

    TextField(
        label = {Text(text = "著者")},
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        value = author, onValueChange = {author = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))

    TextField(
        label = {Text(text = "出版社")},
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        value = publisher, onValueChange = {publisher = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))


    var checkedTagNum by remember { mutableStateOf(-1) }
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(10.dp))
        RadioButton(selected = checkedTagNum==-1, onClick = { checkedTagNum = -1 })
        Text(text = "タグなし")
        tagSet.forEachIndexed {index, it ->
            Spacer(modifier = Modifier.size(10.dp))
            RadioButton(selected = checkedTagNum == index, onClick = { checkedTagNum = index })
            Text(text = "#$it")
        }
    }

    OutlinedButton(
        onClick = {
            val searchMap = mutableMapOf<String, String>()
            if(title.isNotBlank()){
                searchMap["title"] = title
            }
            if(author.isNotBlank()){
                searchMap["author"] = author
            }
            if(publisher.isNotBlank()){
                searchMap["publisher"] = publisher
            }
            if(checkedTagNum > -1){
                searchMap["tag"] = tagSet.elementAt(checkedTagNum)
            }
            if(searchMap.isNotEmpty()) {
                doSearch(searchMap.entries.joinToString())
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
    ) {
        Text(text = "検索", Modifier)
    }

}
