package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.isoffice.bookshelfsharing.R
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookInfo
import com.isoffice.bookshelfsharing.ui.viewModel.BookListState
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


@Composable
fun MainScreen(
    navController:NavHostController,
    booksViewModel: BooksViewModel,
    scrollViewModel: ScrollViewModel
) {
    val state by booksViewModel.state.collectAsState()
    booksViewModel.getAllBooksList()

    MainScreen(
        state,
        { navController.navigate("barcode") },
        { navController.navigate("bookDetail/$it")},
        { navController.navigate("titleSearch/$it")},
        { booksViewModel.deleteBook(it) },
        { scrollViewModel.setScrollIndex(it)},
        { navController.navigate("inputISBN")},
        { navController.navigate("inputBook")},
        scrollViewModel.state.collectAsState().value.scrollIndex
    )
}

@Composable
fun MainScreen(
    state: BookListState,
    onNavigateToBarcode: () -> Unit,
    onNavigateToDetail:(str:String)->Unit,
    onSearchTitle:(title:String) -> Unit,
    onClickDelete:(key:String)->Unit,
    setScrollIndex:(index:Int)->Unit,
    onNavigateToInputCode:() -> Unit,
    onNavigateToInputBook:() -> Unit,
    index:Int
) {

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val bookList = state.bookList?.toMutableList()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Column {
                Box(modifier = Modifier
                    .height(40.dp)
                    .background(colorResource(id = R.color.brown_1)))
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick =  onNavigateToInputCode ) {
                    Text(text="ISBN???????????????")
                }
                TextButton(onClick = onNavigateToInputBook) {
                    Text(text="??????????????????")
                }
            }
        },
        topBar = {
            AppBar(onSearchTitle, scaffoldState, scope)
        },
        floatingActionButton = { FloatingCameraButton(onNavigateToBarcode) },
    ) { padding->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            if(bookList == null){
                CircleProgressIndicator()
            } else if(bookList.isEmpty()) {
               Column {}
            } else {
                MainContent(
                    bookList,
                    onNavigateToDetail,
                    onClickDelete,
                    setScrollIndex,
                    index
                )
            }
        }
    }
}

@Composable
fun CircleProgressIndicator() {
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
    onSearchTitle:(title:String) -> Unit,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
){
    TopAppBar(
        title = { SearchBar(onSearchTitle)},
        navigationIcon = {
            IconButton(onClick = {
                scope.launch {
                    scaffoldState.drawerState.apply {
                        if(isClosed) open() else close()
                    }
                }
            }) {
                Icon(Icons.Filled.Menu, contentDescription = "Open drawer")
            }
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
private fun SearchBar(
    onSearchTitle:(title:String) -> Unit
){
    var text by rememberSaveable { mutableStateOf("") }

    TextField(
        value = text,
        textStyle = TextStyle(color = Color.Black),
        onValueChange = {text = it},
        placeholder = { Text(text = "??????????????????")},
        trailingIcon = {
            IconButton(onClick = {
                onSearchTitle(text)
            }) {
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
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchTitle(text) },
        )
    )
}


@Composable
fun MainContent(
    bookList: MutableList<BookInfo>,
    onNavigateToDetail:(key:String)->Unit,
    onClickDelete:(key:String)->Unit,
    setScrollIndex:(index:Int)->Unit,
    index:Int
) {
    val listScrollState = rememberLazyListState(index)

    BoxWithConstraints {
        val screenWidth = with(LocalDensity.current) {constraints.maxWidth.toDp()}
        val textWidth = (screenWidth - 150.dp )
        LazyColumn(
            state = listScrollState,
            modifier = Modifier.background(Color.White)
        ) {
            items(bookList){
                BookList(it, textWidth,onNavigateToDetail,onClickDelete,setScrollIndex,listScrollState)
            }
        }
    }
}

@Composable
fun BookList(
    bookInfo: BookInfo,
    textWidth: Dp,
    onNavigateToDetail:(str:String)->Unit,
    onClickDelete:(key:String)->Unit,
    setScrollIndex:(index:Int)->Unit,
    listScrollState: LazyListState
){
    val id = bookInfo.key
    val book = bookInfo.book
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
            .clickable(onClick = {
                setScrollIndex(listScrollState.firstVisibleItemIndex)
                onNavigateToDetail(id)
            })
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
            Text(text = "????????????${book.publisher.toString()}")
            Text(text = "????????????${book.publishedDate.toString()}")

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
        DeleteConfirm(book,onClickDelete)
    }

}

@Composable
private fun DeleteConfirm(
    book: Book,
    onClickDelete:(key:String)->Unit
){
    val openDialog = remember{ mutableStateOf(true) }
    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                Text(text = "???${book.title}???????????????????????????")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    deleteBook(book.isbn!!,onClickDelete)
                }) {
                    Text("??????")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog.value = false
                }) {
                    Text("?????????")
                }
            }
        )
    }
}


private fun deleteBook(
    isbn:String,
    onClickDelete:(key:String)->Unit
){
    onClickDelete(isbn)
}
