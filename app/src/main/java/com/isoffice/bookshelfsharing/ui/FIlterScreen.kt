package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun FilterScreen(
    navController: NavHostController,
) {
    Scaffold(
        topBar = {TopBar{ navController.navigate("main") }}
    ){
        padding ->
        Column(
            Modifier
                .padding(3.dp)
                .fillMaxWidth(),
        ) {
            Main{ navController.navigate("detailedSearch/$it") }
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
private fun Main(doSearch:(str:String)->Unit){
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("")}
    var tag by remember { mutableStateOf("") }

    TextField(
        label = {Text(text = "タイトル")},
        modifier = Modifier.fillMaxWidth().padding(3.dp),
        value = title, onValueChange = {title = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))

    TextField(
        label = {Text(text = "著者")},
        modifier = Modifier.fillMaxWidth().padding(3.dp),
        value = author, onValueChange = {author = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))

    TextField(
        label = {Text(text = "出版社")},
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        value = publisher, onValueChange = {publisher = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))

    TextField(
        label = {Text(text = "タグ")},
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        value = tag, onValueChange = {tag = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))

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
            if(tag.isNotBlank()){
                searchMap["tag"] = tag
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
