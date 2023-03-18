package com.isoffice.bookshelfsharing.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.R
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.OpenBD
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
            .padding(3.dp)
            .fillMaxWidth(),
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
            label = {Text("ふりがな")},
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
            Text(text = "本棚登録", Modifier)
        }
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
