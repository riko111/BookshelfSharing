package com.isoffice.bookshelfsharing.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.isoffice.bookshelfsharing.R

import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.dao.BookOpenBDMapper
import com.isoffice.bookshelfsharing.dao.GoogleBooksMapper
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookHttp
import com.isoffice.bookshelfsharing.model.GoogleBooks
import com.isoffice.bookshelfsharing.model.OpenBD
import com.isoffice.bookshelfsharing.ui.viewModel.BookState
import com.isoffice.bookshelfsharing.ui.viewModel.BookViewModel
import kotlinx.coroutines.*

/*
　APIからの検索画面
 */
@Composable
fun BookContentScreen(
    navController: NavHostController,
    barcode: String,
    user: FirebaseUser,
    bookViewModel: BookViewModel
) {
    val state by bookViewModel.state.collectAsState()
    BookContentScreen(
        barcode,
        user,
        state,
        {bookViewModel.getBookByIsbn(it)},
        {navController.navigate("inputBook/$it")},
        {bookViewModel.addBook(it)},
        {navController.navigate("main")},
        {navController.navigate("bookDetail/$it")}
    )
}

@Composable
fun BookContentScreen(
    barcode: String,
    user:FirebaseUser,
    state: BookState,
    onSearchIsbn:(isbn:String) ->Unit,
    onNavigateToInputBook:(isbn:String) -> Unit,
    onRegisterBook:(book:Book) ->Unit,
    onNavigateToMain:() -> Unit,
    onNavigateToDetail:(str:String)->Unit,
){
    onSearchIsbn(barcode)
    val bookInfo = state.book
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxSize(),
    ) {
        if(bookInfo?.value != null) {    // 本棚にあるときは本棚から情報を持ってくる
            val book = bookInfo.value!!.book
            val painter = if(book.thumbnail != null && book.thumbnail != "") {
                rememberAsyncImagePainter(book.thumbnail)
            } else {
                painterResource(id = R.drawable.ic_broken_image)
            }

            BookContent(
                painter,
                book.title,
                book.subtitle,
                book.author,
                book.publisher,
                book.publishedDate,
            )
            OutlinedButton(
                onClick = {onNavigateToDetail(bookInfo.value!!.key)},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)) {
                Text(text = "本棚にあります", Modifier)
            }
        } else {    // 本棚にないときはOpenBDを検索
            var openBD: OpenBD? = null
            runBlocking {
                val job = launch {
                    openBD = withContext(Dispatchers.IO) {
                        BookHttp.searchBook(barcode)
                    }
                }
                job.join()
            }
            if (openBD == null) { // それでもないときはGoogleBooksAPIを検索

                var googleBooks: GoogleBooks? = null
                runBlocking {
                    val job = launch {
                        googleBooks = withContext(Dispatchers.IO) {
                            BookHttp.searchBookByGoogle(barcode)
                        }
                    }
                    job.join()
                }

                if(googleBooks == null || googleBooks!!.items == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "「$barcode」に該当する書籍がありません")
                        OutlinedButton(
                            onClick = { onNavigateToInputBook(barcode) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp)
                        ) {
                            Text(text = "手動本棚登録")
                        }
                    }
                }else {
                    var showDialog by remember { mutableStateOf(false) }
                    val book = GoogleBooksMapper.GooglsBookToBook(googleBooks!!,user)
                    BookContent(book)
                    OutlinedButton(
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(3.dp)
                    ) {
                        Text(text = "本棚登録", Modifier)
                    }
                    if(showDialog){
                        RegisteredAlert(book, onRegisterBook, onNavigateToMain,context)
                    }
                }
            } else {
                val book = BookOpenBDMapper.openBDToBook(openBD!!, user)
                var showDialog by remember { mutableStateOf(false) }
                BookContent(book)
                OutlinedButton(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                ) {
                    Text(text = "本棚登録", Modifier)
                }
                if(showDialog){
                    RegisteredAlert(book, onRegisterBook, onNavigateToMain,context)
                }
            }
        }
    }
}
@Composable
private fun BookContent(
    book: Book
) {
    val painter = if(book.thumbnail != null && book.thumbnail != "") {
        rememberAsyncImagePainter(book.thumbnail)
    } else {
        painterResource(id = R.drawable.ic_broken_image)
    }

    BookContent(
        painter,
        book.title,
        book.subtitle,
        book.author,
        book.publisher,
        book.publishedDate,
    )
}
@Composable
private fun BookContent(
    painter:Painter,
    title: String ,
    subtitle: String?,
    author: String?,
    publisher:String?,
    publishedDate: String?,
) {

    Column (modifier = Modifier
        .padding(vertical = 2.dp)
        .fillMaxWidth()
        .width(IntrinsicSize.Max)
        .border(1.dp, Color.LightGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = title,
                modifier = Modifier.width(150.dp),
                contentScale = ContentScale.Fit,
            )
            Column(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .weight(fill = true, weight = 1f)
            ) {
                Text(text = title, fontWeight = FontWeight.Bold)
                if (subtitle != null) Text(text = subtitle)
                Spacer(modifier = Modifier.size(5.dp))
                if (author != null) Text(text = author)
                if (publisher != null) Text(text = publisher)
                if (publishedDate != null) Text(text = publishedDate)
            }
        }
    }
}

@Composable
private fun RegisteredAlert(book:Book,
                            onRegisterBook:(book:Book) ->Unit,
                            onNavigateToMain:() -> Unit,
                            context: Context
){
    val openDialog = remember{ mutableStateOf(true) }

    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                val title = book.title
                Text(text = "「${title}」を本棚に入れますか？")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    registerBook(book, onRegisterBook, onNavigateToMain,context)
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


private fun registerBook(book:Book,
    onRegisterBook:(book:Book) ->Unit,
    onNavigateToMain:() -> Unit,
    context: Context) {
    runBlocking {
        val job2 = launch {
            onRegisterBook(book)
            onNavigateToMain()
        }
        job2.join()
        Toast.makeText(context,"${book.title}を登録しました",Toast.LENGTH_LONG).show()
    }
}

