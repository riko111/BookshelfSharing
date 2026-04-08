package com.isoffice.bookshelfsharing.ui

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    user: FirebaseUser,
    state: BookState,
    onSearchIsbn: (isbn: String) -> Unit,
    onNavigateToInputBook: (isbn: String) -> Unit,
    onRegisterBook: (book: Book) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToDetail: (str: String) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(barcode) {
        onSearchIsbn(barcode)
    }

    val bookInfo = state.book

    Column(
        modifier = Modifier
            .fillMaxWidth().safeDrawingPadding()
            .fillMaxSize().statusBarsPadding(),
    ) {
        if (bookInfo != null) {
            // ... (Keep existing code for books already in the bookshelf)
            val book = bookInfo.book
            val painter = if(!book.thumbnail.isNullOrEmpty()) {
                rememberAsyncImagePainter(book.thumbnail)
            } else {
                painterResource(id = R.drawable.ic_broken_image)
            }
            BookContent(painter, book.title, book.subtitle, book.author, book.publisher, book.publishedDate)
            OutlinedButton(onClick = {onNavigateToDetail(bookInfo.key)}) {
                Text(text = "本棚にあります")
            }
        } else {
            // --- NEW LOGIC: Fetch both and merge ---
            var combinedBook by remember { mutableStateOf<Book?>(null) }
            var isSearching by remember { mutableStateOf(true) }
            val googleApiKey = context.getString(R.string.google_api_key)


            LaunchedEffect(barcode) {
                withContext(Dispatchers.IO) {
                    val openBD = async { BookHttp.searchBook(barcode) }
                    val googleBooks = async { BookHttp.searchBookByGoogle(barcode, googleApiKey) }

                    val resOpenBD = openBD.await()
                    val resGoogle = googleBooks.await()

                    if (resOpenBD != null || (resGoogle != null && resGoogle.items != null)) {
                        // Merge the data
                        combinedBook = mergeBookData(resOpenBD, resGoogle, user, barcode)
                    }
                }
                isSearching = false
            }

            if (isSearching) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (combinedBook == null) {
                // No results found in either API
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "「$barcode」に該当する書籍がありません")
                    OutlinedButton(onClick = { onNavigateToInputBook(barcode) }) {
                        Text(text = "手動本棚登録")
                    }
                }
            } else {
                // Display combined data
                var showDialog by remember { mutableStateOf(false) }
                BookContent(combinedBook!!)
                OutlinedButton(onClick = { showDialog = true }) {
                    Text(text = "本棚登録")
                }
                if (showDialog) {
                    RegisterBookDialog(
                        combinedBook!!,
                        onRegisterBook = { book ->
                            coroutineScope.launch {
                                onRegisterBook(book)
                                onNavigateToMain()
                                Toast.makeText(context, "${book.title}を登録しました", Toast.LENGTH_LONG).show()
                            }
                        },
                        onDismiss = { showDialog = false }
                    )
                }
            }
        }
    }
}
private fun mergeBookData(
    openBD: OpenBD?,
    googleBooks: GoogleBooks?,
    user: FirebaseUser,
    isbn: String
): Book {
    // Start with base conversion from whichever is available
    val baseBook = if (openBD != null) {
        BookOpenBDMapper.openBDToBook(openBD, user)
    } else {
        GoogleBooksMapper.GooglsBookToBook(googleBooks!!, user)
    }

    // If both exist, supplement the baseBook with Google data
    if (openBD != null && googleBooks?.items != null) {
        val gBook = GoogleBooksMapper.GooglsBookToBook(googleBooks, user)

        return baseBook.copy(
            // Use Google's thumbnail if openBD's is missing
            thumbnail = if (baseBook.thumbnail.isNullOrEmpty()) gBook.thumbnail else baseBook.thumbnail,
            // Use Google's subtitle if openBD's is missing
            subtitle = if (baseBook.subtitle.isNullOrEmpty()) gBook.subtitle else baseBook.subtitle,
            // Ensure description is captured (if your Book model has it)
            // description = gBook.description ?: baseBook.description
        )
    }

    return baseBook
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
    painter: Painter,
    title: String?,
    subtitle: String?,
    author: String?,
    publisher: String?,
    publishedDate: String?,
) {

    Column (modifier = Modifier
        .padding(vertical = 2.dp)
        .fillMaxWidth().safeDrawingPadding()
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
                if (title != null) {
                    Text(text = title, fontWeight = FontWeight.Bold)
                }
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
private fun RegisterBookDialog(
    book: Book,
    onRegisterBook: (book: Book) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val title = book.title
            Text(text = "「${title}」を本棚に入れますか？")
        },
        confirmButton = {
            TextButton(onClick = {
                onRegisterBook(book)
                onDismiss()
            }) {
                Text("はい")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("いいえ")
            }
        }
    )
}


// Removed registerBook with runBlocking as it's replaced by coroutineScope.launch above

