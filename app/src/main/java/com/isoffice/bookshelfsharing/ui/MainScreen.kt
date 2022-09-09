package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.R
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.dao.DBAccess
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.ui.viewModel.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber


var  bookList : SnapshotStateList<Book>? = null
@Composable
fun MainScreen(bookDao: BookDao, onNavigateToBarcode:() -> Unit, viewModel: MainViewModel) {
    bookList = remember{ mutableStateListOf() }

    val navigationIcon = (@Composable{IconButton(onClick = { /*TODO*/ }) {
        Icon(Icons.Filled.Menu, contentDescription = "Open drawer")
    }})

    Scaffold(
        topBar = {
            AppBar(navigationIcon)
        },
        floatingActionButton = { FloatingCameraButton(onNavigateToBarcode) },
    ) { padding->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            bookList = bookDao.readBookList()
            if(bookList!!.isEmpty()){
                CircleProgressIndicator()
            }else {
                MainContent(bookList!!, bookDao, viewModel)
            }
        }
    }
}

@Composable
private fun CircleProgressIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FloatingCameraButton(onNavigateToBarcode:() -> Unit) {
    FloatingActionButton(onClick = onNavigateToBarcode) {
        Icon(
            imageVector = Icons.Default.PhotoCamera,
            contentDescription = "Add Books",
            tint = colorResource(id = R.color.brown_1)
        )
    }
}

@Composable
private fun AppBar(
    navigationIcon: @Composable (() -> Unit)
){
    TopAppBar(
        title = { SearchBar()},
        navigationIcon = navigationIcon,
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
private fun SearchBar(){
    var text by rememberSaveable { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = {text = it},
        placeholder = { Text(text = "本棚検索")},
        trailingIcon = {
            IconButton(onClick = { }) {
                Icon(
                    Icons.Sharp.Search,
                    contentDescription = "",
                    tint = colorResource(id = R.color.brown_1)
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
        keyboardOptions = KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Words,
            autoCorrect = false,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {/*search(composableScope,text,state,navController,email!!,bookDao)*/}
        )
    )
}


@Composable
private fun MainContent(bookList: MutableList<Book>, bookDao: BookDao, viewModel: MainViewModel) {
    BoxWithConstraints {
        val screenWidth = with(LocalDensity.current) {constraints.maxWidth.toDp()}
        val textWidth = (screenWidth - 150.dp )
        LazyColumn(
            modifier = Modifier
                .background(Color.White)
                .scrollable(
                    state = rememberScrollState(),
                    orientation = Orientation.Vertical
                )
        ) {
            bookList.forEach() {
                item{BookList(it, textWidth, bookDao, viewModel)}
            }
        }
    }
}

@Composable
fun BookList(book: Book, textWidth: Dp, bookDao: BookDao, viewModel: MainViewModel){
    val painter = if(book.thumbnail != null && book.thumbnail != "") {
        rememberAsyncImagePainter(book.thumbnail)
    } else {
        painterResource(id = R.drawable.ic_broken_image)
    }
    var showDialog2 by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color.DarkGray, shape = RectangleShape)
            .clickable(onClick = { viewModel.navController!!.navigate("bookDetail/${book.isbn!!}") })
    ){
        Image(
            painter = painter,
            contentDescription = book.title,
            modifier = Modifier.size(100.dp),
            contentScale = ContentScale.Fit
        )
        Column(modifier = Modifier
            .padding(vertical = 2.dp)
            .width(textWidth)) {
            Text(text = book.title, fontWeight = FontWeight.Bold)
            Text(text = book.author.toString())
            Text(text = "出版社：${book.publisher.toString()}")
            Text(text = "出版日：${book.publishedDate.toString()}")

        }
        IconButton(onClick = { showDialog2 = true }) {
            Icon(
                Icons.Sharp.Delete,
                contentDescription = "",
                tint = colorResource(id = R.color.brown_1),
                modifier = Modifier.size(25.dp)
            )
        }
    }
    if(showDialog2){
        DeleteConfirm(book = book, bookDao)
    }

}

@Composable
private fun DeleteConfirm(book: Book, bookDao: BookDao){
    val openDialog = remember{ mutableStateOf(true) }
    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                Text(text = "「${book.title}」を削除しますか？")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    deleteBook(book.isbn!!, bookDao)
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

private fun deleteBook(isbn:String, bookDao: BookDao) {
    bookDao.deleteBook(isbn)
    bookList = bookDao.readBookList()
}
