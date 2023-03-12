package com.isoffice.bookshelfsharing.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookInfo
import com.isoffice.bookshelfsharing.model.OpenBD
import com.isoffice.bookshelfsharing.ui.viewModel.BookViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun BookInfoUpdateScreen(
    navController:NavHostController,
    key : String, bookDao: BookDao,
    bookViewModel: BookViewModel,
){
    var info = remember { mutableStateOf<BookInfo?>(null) }
    runBlocking {
        val job = launch {
            info = bookDao.readBook(key)
        }
        job.join()
    }

    val book = info.value!!.book
    val ownerId = book.ownerId
    val ownerIcon = book.ownerIcon

    val thumbnail = book.thumbnail
    var title by remember { mutableStateOf(book.title)}
    var furigana by remember { mutableStateOf(book.furigana)}
    var author by remember { mutableStateOf(if(book.author.isNullOrEmpty()){""}else{book.author})}
    var subtitle by remember { mutableStateOf(if(book.subtitle.isNullOrEmpty()){""}else{book.subtitle}) }
    var description by remember { mutableStateOf(if(book.description.isNullOrEmpty()){""}else{book.description})}
    var publisher by remember { mutableStateOf(if(book.publisher.isNullOrEmpty()){""}else{book.publisher})}
    var publishedDate by remember { mutableStateOf(if(book.publishedDate.isNullOrEmpty()){""}else{book.publishedDate})}
    var isbn by remember { mutableStateOf(if(book.isbn.isNullOrEmpty()){""}else{book.isbn})}
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(3.dp)
            .fillMaxWidth(),
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            label = {Text("タイトル*")},
            value = title,
            onValueChange = {title = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            label = {Text("フリガナ")},
            value = furigana, onValueChange = {furigana = it},
            placeholder = { Text(text = "フリガナ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            label = {Text("サブタイトル")},
            value = subtitle, onValueChange = {subtitle = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            label = {Text("著者*")},
            value = author, onValueChange = {author = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            label = {Text("情報")},
            value = description, onValueChange = {description = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp),
        label = {Text("出版社")},
        value = publisher, onValueChange = {publisher = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            label = {Text("出版日")},
            value = publishedDate,
            placeholder = { Text(text = "yyyymmdd") },
            onValueChange = {publishedDate = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(
            label = {Text("ISBN")},
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp),
            value = isbn, onValueChange = {isbn = it}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        OutlinedButton(
            onClick = { showDialog = true  },
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
        ) {
            Text(text = "更新", Modifier)
        }
    }
    if(showDialog){
        if(furigana.isEmpty()) furigana = ""
        if(subtitle.isEmpty()) subtitle = ""
        if(description.isEmpty()) description = ""
        if(publisher.isEmpty()) publisher = ""
        if(publishedDate.isEmpty()) publishedDate = ""
        if(isbn.isEmpty()) isbn = ""

        val updateBook = Book(
            title = title,
            furigana = furigana,
            author = author,
            subtitle = subtitle,
            description = description,
            publisher = publisher,
            publishedDate = publishedDate,
            isbn = isbn,
            ownerId = ownerId,
            ownerIcon = ownerIcon,
            thumbnail = thumbnail
        )

        RegisteredAlert(updateBook, { bookViewModel.updateBook(key,it) }, {navController.navigate("main")})
    }
}

@Composable
private fun RegisteredAlert(book: Book, onUpdateBook:(book:Book) ->Unit, onNavigateToMain:()->Unit){
    val openDialog = remember{ mutableStateOf(true) }
    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                Text(text = "「${book.title}」を更新しますか？")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    updateBook(book, onUpdateBook,onNavigateToMain)
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

private fun updateBook(book:Book, onUpdateBook: (book: Book) -> Unit,onNavigateToMain: () -> Unit){
    onUpdateBook(book)
    onNavigateToMain()
}
