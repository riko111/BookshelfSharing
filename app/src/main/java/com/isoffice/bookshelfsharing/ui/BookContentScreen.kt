package com.isoffice.bookshelfsharing.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CameraEnhance
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
import coil.compose.AsyncImage
import com.isoffice.bookshelfsharing.R

import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.dao.BookOpenBDMapper
import com.isoffice.bookshelfsharing.dao.GoogleBooksMapper
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookHttp
import com.isoffice.bookshelfsharing.model.GoogleBooks
import com.isoffice.bookshelfsharing.model.OpenBD
import com.isoffice.bookshelfsharing.ui.compose.BookInfoFormCompose
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
        { navController.navigate("barcode") },
        bookViewModel,
        navController,
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
    onNavigateToBarcode: () -> Unit,
    bookViewModel: BookViewModel,
    navController: NavHostController,
    onNavigateToDetail: (str: String) -> Unit,
) {
    val context = LocalContext.current

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
                        combinedBook = mergeBookData(resOpenBD, resGoogle, user)
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
                // 書籍が見つかったときは登録画面
                BookContent(combinedBook!!, user, bookViewModel, navController, onNavigateToBarcode)
            }
        }
    }
}
private fun mergeBookData(
    openBD: OpenBD?,
    googleBooks: GoogleBooks?,
    user: FirebaseUser,
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
            thumbnail = if (baseBook.thumbnail.isNullOrEmpty()) gBook.thumbnail else baseBook.thumbnail,
            subtitle = if (baseBook.subtitle.isNullOrEmpty()) gBook.subtitle else baseBook.subtitle,
            description = if (baseBook.description.isNullOrEmpty()) gBook.description else baseBook.description,
            publisher = if (baseBook.publisher.isNullOrEmpty()) gBook.publisher else baseBook.publisher,
        )
    }

    return baseBook
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
private fun BookContent(
    book: Book,
    user: FirebaseUser,
    bookViewModel: BookViewModel,
    navController: NavHostController,
    onNavigateToBarcode: () -> Unit,
) {
    val painter = if(book.thumbnail != null && book.thumbnail != "") {
        rememberAsyncImagePainter(book.thumbnail)
    } else {
        painterResource(id = R.drawable.ic_broken_image)
    }

    val thumbnail = remember { mutableStateOf(book.thumbnail) }
    var title by remember { mutableStateOf(book.title ?: "") }
    var furigana by remember { mutableStateOf(book.furigana ?: "") }
    var author by remember { mutableStateOf(book.author ?: "") }
    var subtitle by remember { mutableStateOf(book.subtitle ?: "") }
    var description by remember { mutableStateOf(book.description ?: "") }
    var publisher by remember { mutableStateOf(book.publisher ?: "") }
    var publishedDate by remember { mutableStateOf(book.publishedDate ?: "") }
    var isbn by remember { mutableStateOf(book.isbn ?: "") }
    var showDialog by remember { mutableStateOf(false) }
    var uri: Uri? = null

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri = uri
            }
        }
    )
    if (imageUri != null) {
        AsyncImage(
            model = imageUri, contentDescription = "BookSharing",
            modifier = Modifier.size(200.dp),
        )
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(3.dp)
            .fillMaxWidth().safeDrawingPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
        ){
            Image(
                painter = painter,
                contentDescription = "",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit,
            )
            IconButton(onClick = {
                val tmpUri = getImageUri(context = context)
                uri = tmpUri
                cameraLauncher.launch(tmpUri)
                thumbnail.value = uri.toString()
            }) {
                Icon(Icons.Sharp.CameraEnhance, contentDescription = "")
            }
        }

        BookInfoFormCompose(
            title = title,
            onTitleChange = { title = it },
            furigana = furigana,
            onFuriganaChange = { furigana = it },
            subtitle = subtitle,
            onSubtitleChange = { subtitle = it },
            author = author,
            onAuthorChange = { author = it },
            description = description,
            onDescriptionChange = { description = it },
            publisher = publisher,
            onPublisherChange = { publisher = it },
            publishedDate = publishedDate,
            onPublishedDateChange = { publishedDate = it },
            isbn = isbn,
            onIsbnChange = { isbn = it },
            furiganaLabel = "ふりがな",
            submitLabel = "本棚登録",
            onSubmit = { showDialog = true }
        )
    }
    if(showDialog){
        if(furigana.isEmpty()) furigana = ""
        if(subtitle.isEmpty()) subtitle = ""
        if(description.isEmpty()) description = ""
        if(publisher.isEmpty()) publisher = ""
        if(publishedDate.isEmpty()) publishedDate = ""
        if(isbn.isEmpty()) isbn = ""

        val book = Book(
            title = title,
            furigana = furigana,
            author = author,
            subtitle = subtitle,
            description = description,
            publisher = publisher,
            publishedDate = publishedDate,
            isbn = isbn,
            ownerId = user.email,
            thumbnail = thumbnail.value,
            ownerIcon = user.photoUrl.toString()
        )

        InputBookDialog(book, { bookViewModel.addBook(it) }, {navController.navigate("main")}, onNavigateToBarcode)
    }
}



@Composable
private fun InputBookDialog(book: Book, onRegisterBook:(book:Book) ->Unit, onNavigateToMain:()->Unit,
                            onNavigateToBarcode: () -> Unit,){
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
                Row{
                    TextButton(onClick = {
                        openDialog.value = false
                    }) {
                        Text("いいえ")
                    }
                    TextButton(onClick = {
                        openDialog.value = false
                        registerAndNextSearch(book, onRegisterBook,onNavigateToBarcode)
                    }) {
                        Text("はい（次の本）")
                    }
                }
            }
        )
    }
}

private fun registerBook(book:Book, onRegisterBook: (book: Book) -> Unit,onNavigateToMain: () -> Unit){
    onRegisterBook(book)
    onNavigateToMain()
}
private fun registerAndNextSearch(book:Book, onRegisterBook: (book: Book) -> Unit,onNavigateToBarcode: () -> Unit){
    onRegisterBook(book)
    onNavigateToBarcode()
}