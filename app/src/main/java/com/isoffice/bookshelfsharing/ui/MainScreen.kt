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
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.BookInfo
import com.isoffice.bookshelfsharing.ui.localData.getOtherBooksList
import com.isoffice.bookshelfsharing.ui.localData.getUserBooksList
import com.isoffice.bookshelfsharing.ui.viewModel.BooksViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.ScrollViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun MainScreen(
    navController:NavHostController,
    user: String,
    bookDao: BookDao,
    scrollViewModel: ScrollViewModel
) {
    MainScreen(
        user,
        bookDao,
        { navController.navigate("barcode") },
        { navController.navigate("bookDetail/$it")},
        { navController.navigate("titleSearch/$it")},
        { navController.navigate("filter")},
        { BooksViewModel(bookDao).deleteBook(it) },
        { scrollViewModel.setScrollIndex(it)},
        { navController.navigate("inputISBN")},
        { navController.navigate("inputBook")},
        scrollViewModel.state.collectAsState().value.scrollIndex
    )
}


@Composable
fun MainScreen(
    user: String,
    bookDao: BookDao,
    onNavigateToBarcode: () -> Unit,
    onNavigateToDetail:(str:String)->Unit,
    onSearchTitle:(title:String) -> Unit,
    onFilter: () -> Unit,
    onClickDelete:(key:String)->Unit,
    setScrollIndex:(index:Int)->Unit,
    onNavigateToInputCode:() -> Unit,
    onNavigateToInputBook:() -> Unit,
    index:Int
) {

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var selectedTabIndex by rememberSaveable{mutableStateOf(Books.MINE)}
    val checkedState = remember { mutableStateOf(false) }

    val booksViewModel = BooksViewModel(bookDao)
    booksViewModel.getAllBooksList()

    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            Column {
                Box(modifier = Modifier
                    .height(40.dp)
                    .background(colorResource(id = R.color.brown_1)))
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick =  onNavigateToInputCode ) {
                    Text(text="ISBN手入力検索")
                }
                TextButton(onClick = { onNavigateToInputBook() }) {
                    Text(text="手動本棚登録")
                }
            }
        },
        topBar = {
            AppBar(onSearchTitle, onFilter, scaffoldState, scope)
        },
        bottomBar = {
            DeleteSetBar(checkedState)
        },
        floatingActionButton = { FloatingCameraButton(onNavigateToBarcode) },
    ) { padding->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex.ordinal,
            ) {
                Books.values().map { it.title }.forEachIndexed{index, value ->
                    Tab(text={Text(text = value)},
                        selected = selectedTabIndex.ordinal == index,
                        onClick = { selectedTabIndex = Books.values()[index] })
                }
            }
            if(booksViewModel.booksState.bookList.isEmpty()){
                CircleProgressIndicator()
            } else {
                MainContent(
                    user,
                    selectedTabIndex,
                    checkedState,
                    booksViewModel,
                    onNavigateToDetail,
                    onClickDelete,
                    setScrollIndex,
                    index
                )
            }
        }
    }
}

enum class Books(val title:String){
    MINE("自分の"), OTHERS("他の")
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
    onFilter:()->Unit,
    scaffoldState: ScaffoldState,
    scope: CoroutineScope
){
    TopAppBar(
        title = { SearchBar(onSearchTitle,onFilter)},
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
    onSearchTitle:(title:String) -> Unit,
    onFilter:() -> Unit,
){
    Row{
        SearchBar(onSearchTitle)
        FilterButton (onFilter)
    }
}

@Composable
private fun FilterButton(
    onFilter:() -> Unit,
){
    IconButton(
        onClick = { onFilter() },
    ) {
        Icon(Icons.Filled.FilterList, contentDescription = "絞り込み")
    }
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
        placeholder = { Text(text = "タイトル検索")},
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
private fun MainContent(
    user: String,
    selectedTabIndex:Books,
    checkedState: MutableState<Boolean>,
    booksViewModel: BooksViewModel,
    onNavigateToDetail:(key:String)->Unit,
    onClickDelete:(key:String)->Unit,
    setScrollIndex:(index:Int)->Unit,
    index:Int
) {
    val list = if(selectedTabIndex == Books.MINE) {
        getUserBooksList(user, booksViewModel.booksState.bookList,checkedState.value)
    } else {
        getOtherBooksList(user,booksViewModel.booksState.bookList,checkedState.value)
    }
    //val bookState = booksViewModel.booksState

    MainContent(
        list,
        onNavigateToDetail,
        onClickDelete,
        setScrollIndex,
        index
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
            state = listScrollState
        ) {
            items(bookList){

                Column(
                    modifier = if(it.book.deleteFlag){
                        Modifier.background(Color.Gray)
                    } else {
                        Modifier.background(Color.White)
                    }
                ) {
                   BookList( it, textWidth,onNavigateToDetail,onClickDelete,setScrollIndex,listScrollState)
                }
            }
        }
    }
}

@Composable
fun DeleteSetBar(checkedState:MutableState<Boolean>){
    Row(Modifier.background(MaterialTheme.colors.primary)) {
        Switch(
            checked = checkedState.value,
            onCheckedChange = {checkedState.value = it},
            colors = SwitchDefaults.colors(checkedThumbColor = colorResource(id = R.color.brown_2))
        )
        Text(text = "処分した本も含める", modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.CenterVertically))
    }
}



@Composable
fun BookList(
//    bookList: MutableList<BookInfo>,
    bookInfo: BookInfo,
    textWidth: Dp,
    onNavigateToDetail:(str:String)->Unit,
    onClickDelete:(key:String)->Unit,
    setScrollIndex:(index:Int)->Unit,
    listScrollState: LazyListState
){
    val id = bookInfo.key
    val book = bookInfo.book
    val deleteFlag = book.deleteFlag
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
            Text(text = "出版社：${book.publisher.toString()}")
            Text(text = "出版日：${book.publishedDate.toString()}")

        }
        if(!deleteFlag) {
            IconButton(onClick = { showDialog2 = true }) {
                Icon(
                    Icons.Sharp.Delete,
                    contentDescription = "",
                    tint = colorResource(id = R.color.brown_1),
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
    if(showDialog2){
        DeleteConfirm(bookInfo,onClickDelete)
    }

}

@Composable
private fun DeleteConfirm(
    //bookList: MutableList<BookInfo>,
    bookInfo: BookInfo,
    onClickDelete:(key:String)->Unit
){
    val book = bookInfo.book
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
                    deleteBook(bookInfo, onClickDelete)
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


private fun deleteBook(
    bookInfo: BookInfo,
    onClickDelete:(key:String)->Unit
){
    onClickDelete(bookInfo.key)
}


