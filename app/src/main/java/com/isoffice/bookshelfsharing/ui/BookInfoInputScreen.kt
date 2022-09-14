package com.isoffice.bookshelfsharing.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.OpenBD
import com.isoffice.bookshelfsharing.ui.viewModel.BookViewModel

@Composable
fun BookInfoInputScreen(
    navController:NavHostController,
    user: FirebaseUser,
    bookViewModel: BookViewModel
){
    var title by remember { mutableStateOf("")}
    var furigana by remember { mutableStateOf("")}
    var author by remember { mutableStateOf("")}
    var subtitle by remember { mutableStateOf("")}
    var publisher by remember { mutableStateOf("")}
    var publishedDate by remember { mutableStateOf("")}
    var isbn by remember { mutableStateOf("")}
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "タイトル",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.08f)
            )
            TextField(value = title, onValueChange = {title = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "フリガナ",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.08f)
            )
            TextField(
                value = furigana, onValueChange = {furigana = it},
                placeholder = { Text(text = "フリガナ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "サブタイトル",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.08f)
            )
            TextField(value = subtitle, onValueChange = {subtitle = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "著者",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.08f)
            )
            TextField(value = author, onValueChange = {author = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "出版社",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.08f)
            )
            TextField(value = publisher, onValueChange = {publisher = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "出版日",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.08f)
            )
            TextField(value = publishedDate, onValueChange = {publishedDate = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "ISBN",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(0.08f)
            )
            TextField(value = isbn, onValueChange = {isbn = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        OutlinedButton(
            onClick = { showDialog = true  },
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
        ) {
            Text(text = "本棚登録", Modifier)
        }
    }
    if(showDialog){
        val book = Book(
            title = title,
            author = author,
            subtitle = subtitle,
            publisher = publisher,
            publishedDate = publishedDate,
            isbn = isbn,
            ownerId = user.email,
            ownerIcon = user.photoUrl.toString()
        )

        RegisteredAlert(book, { bookViewModel.addBook(it) }, {navController.navigate("main")})
    }
}




@Composable
private fun RegisteredAlert(book: Book, onRegisterBook:(book:Book) ->Unit, onNavigateToMain:()->Unit){
    val openDialog = remember{ mutableStateOf(true) }
    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                Text(text = "「${book.title}」を本棚に入れますか？")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    registerBook(book, onRegisterBook,onNavigateToMain)
                }) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog.value = false
                }) {
                    Text("いいえ")
                }
            }
        )
    }
}

private fun registerBook(book:Book, onRegisterBook: (book: Book) -> Unit,onNavigateToMain: () -> Unit){
    onRegisterBook(book)
    onNavigateToMain()
}
