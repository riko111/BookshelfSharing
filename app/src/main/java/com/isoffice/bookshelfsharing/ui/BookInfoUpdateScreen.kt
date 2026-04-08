package com.isoffice.bookshelfsharing.ui

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CameraEnhance
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.isoffice.bookshelfsharing.R
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookInfo
import com.isoffice.bookshelfsharing.ui.viewModel.*

@Composable
fun BookInfoUpdateScreen(
    navController:NavHostController,
    key : String, bookDao: BookDao,
    bookViewModel: BookViewModel,
){
    var info by remember { mutableStateOf<BookInfo?>(null) }

    LaunchedEffect(key) {
        bookDao.readBook(key) {
            info = it
        }
    }

    val currentInfo = info
    if (currentInfo == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        BookInfoUpdateContent(
            navController = navController,
            key = key,
            currentInfo = currentInfo,
            bookViewModel = bookViewModel
        )
    }
}

@Composable
fun BookInfoUpdateContent(
    navController: NavHostController,
    key: String,
    currentInfo: BookInfo,
    bookViewModel: BookViewModel
) {
    val book = currentInfo.book
    val ownerId = book.ownerId
    val ownerIcon = book.ownerIcon

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

    val painter = if (thumbnail.value != null) {
        rememberAsyncImagePainter(thumbnail.value)
    } else {
        painterResource(id = R.drawable.ic_broken_image)
    }

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data ?: uri
            }
        }
    )
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
        ) {
            Image(
                painter = painter,
                contentDescription = book.title,
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

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            label = { Text("タイトル*") },
            value = title,
            onValueChange = { title = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            label = { Text("フリガナ") },
            value = furigana, onValueChange = { furigana = it },
            placeholder = { Text(text = "フリガナ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            label = { Text("サブタイトル") },
            value = subtitle,
            onValueChange = { subtitle = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            label = { Text("著者*") },
            value = author,
            onValueChange = { author = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            label = { Text("情報") },
            value = description,
            onValueChange = { description = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            label = { Text("出版社") },
            value = publisher,
            onValueChange = { publisher = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            label = { Text("出版日") },
            value = publishedDate,
            placeholder = { Text(text = "yyyymmdd") },
            onValueChange = { publishedDate = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            label = { Text("ISBN") },
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp),
            value = isbn,
            onValueChange = { isbn = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth().safeDrawingPadding()
                .padding(3.dp)
        ) {
            Text(text = "更新", Modifier)
        }
    }

    if (showDialog) {
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
            thumbnail = thumbnail.value
        )

        UpdateBookDialog(
            book = updateBook,
            onUpdateBook = { bookViewModel.updateBook(key, it) },
            onNavigateToMain = { navController.navigate("main") }
        )
    }
}

@Composable
fun UpdateBookDialog(
    book: Book,
    onUpdateBook: (Book) -> Unit,
    onNavigateToMain: () -> Unit
) {
    var openDialog by remember { mutableStateOf(true) }
    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            title = {
                Text(text = "「${book.title}」を更新しますか？")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog = false
                    onUpdateBook(book)
                    onNavigateToMain()
                }) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog = false
                }) {
                    Text("いいえ")
                }
            }
        )
    }
}
