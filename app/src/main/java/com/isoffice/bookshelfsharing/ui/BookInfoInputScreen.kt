package com.isoffice.bookshelfsharing.ui

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.R
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.ui.compose.BookInfoFormCompose
import com.isoffice.bookshelfsharing.ui.viewModel.BookViewModel

@Composable
fun BookInfoInputScreen(
    navController:NavHostController,
    user: FirebaseUser,
    barcode:String,
    bookViewModel: BookViewModel
){

    var title by remember { mutableStateOf("")}
    var furigana by remember { mutableStateOf("")}
    var author by remember { mutableStateOf("")}
    var subtitle by remember { mutableStateOf("")}
    var description by remember { mutableStateOf("")   }
    var publisher by remember { mutableStateOf("")}
    var publishedDate by remember { mutableStateOf("")}
    var isbn by remember { mutableStateOf(barcode)}
    var showDialog by remember { mutableStateOf(false) }
    var thumbnail by remember { mutableStateOf("")}
    var uri: Uri? = null

    val painter = if(thumbnail.isNotBlank()) {
        rememberAsyncImagePainter(thumbnail)
    } else {
        painterResource(id = R.drawable.ic_broken_image)
    }

    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {result:  ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
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


    if(imageUri != null){
        AsyncImage(
            model = imageUri,
            contentDescription = "BookSharing",
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
//                launcher.launch(createChooser(tmpUri))
                cameraLauncher.launch(tmpUri)
                thumbnail = uri.toString()
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
            thumbnail = thumbnail,
            ownerIcon = user.photoUrl.toString()
        )

        InputBookDialog(book, { bookViewModel.addBook(it) }, {navController.navigate("main")})
    }
}




@Composable
private fun InputBookDialog(book: Book, onRegisterBook:(book:Book) ->Unit, onNavigateToMain:()->Unit){
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
