package com.isoffice.bookshelfsharing.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.flowlayout.FlowRow
import com.google.common.io.Files.append
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.BookInfo
import com.isoffice.bookshelfsharing.ui.viewModel.MainViewModel
import com.isoffice.bookshelfsharing.ui.viewModel.TagsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/*　本棚の本の詳細 */
@Composable
fun BookDetailScreen(
    key : String, bookDao: BookDao,viewModel: MainViewModel,
){
    var book = remember { mutableStateOf<BookInfo?>(null) }
    runBlocking {
        val job = launch {
            book = bookDao.readBook(key)
        }
        job.join()
    }

    Column(
        if (book.value != null && book.value!!.book.deleteFlag) {
            Modifier.background(Color.Gray)
        } else {
            Modifier.background(Color.White)
        }
            .padding(2.dp)
            .fillMaxSize()
            ) {
        if(book.value == null){
            Box(modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }else {
            BookContent(book,  bookDao, viewModel, )
        }
    }
}

@Composable
private fun BookContent(
    bookInfo: MutableState<BookInfo?>,
    bookDao: BookDao,
    viewModel: MainViewModel,
) {
    val book = bookInfo.value!!.book
    var showDialog by remember { mutableStateOf(false) }

    val painter = if(book.thumbnail != null && book.thumbnail != "") {
        rememberAsyncImagePainter(book.thumbnail)
    } else {
        painterResource(id = com.isoffice.bookshelfsharing.R.drawable.ic_broken_image)
    }
    val icon = if(book.ownerIcon != null && book.ownerIcon != ""){
        rememberAsyncImagePainter(model = book.ownerIcon)
    } else {
        painterResource(id = com.isoffice.bookshelfsharing.R.drawable.ic_broken_image)
    }

    Column(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Row(
            modifier = Modifier
                .padding(vertical = 2.dp)
        ){
            Image(
                painter = painter,
                contentDescription = book.title,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.size(3.dp))
            Column(
                verticalArrangement = Arrangement.Center,){
                Image(
                    painter = icon, contentDescription = book.ownerId,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            }
        }

        Text(text = book.title, fontWeight = FontWeight.Bold)

        if (book.subtitle != null) Text(text = book.subtitle)
        Spacer(modifier = Modifier.size(5.dp))
        Text(text = book.author.toString())
        Text(text = book.publisher.toString())
        if (book.publishedDate != null) Text(text = book.publishedDate)
        if (book.description != null) {
            Text(text = book.description,
                maxLines = 10,)
        }

        TagCompose(bookInfo, bookDao, ) { viewModel.navController!!.navigate("tagSearch/$it") }

        Spacer(modifier = Modifier.padding(20.dp))
        Row{
            if(!book.deleteFlag) {
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        Icons.Sharp.Delete,
                        contentDescription = "",
                        tint = colorResource(id = com.isoffice.bookshelfsharing.R.color.brown_1),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
            IconButton(onClick = { viewModel.navController!!.navigate("bookInfoEdit/${bookInfo.value!!.key}")  }) {
                Icon(
                    Icons.Sharp.Edit,
                    contentDescription = "",
                    tint = colorResource(id = com.isoffice.bookshelfsharing.R.color.brown_1),
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }

    if(showDialog){
        DeleteConfirm(bookInfo.value!!, bookDao, viewModel.navController!!)
    }
}

@Composable
private fun TagCompose(
    bookInfo: MutableState<BookInfo?>,
    bookDao: BookDao,
    onNavigateToTagSearch: (tag: String) -> Unit
){
    val tagsState = rememberTagsState()
    bookInfo.value!!.book.tags?.let { tagsState.replaceAllSet(it) }

    var showDeleteTagDialog by remember { mutableStateOf(false)}
    var showTagDialog by remember { mutableStateOf(false)}
    val tag = remember {mutableStateOf("")}
    Spacer(modifier = Modifier.size(5.dp))
    FlowRow(modifier = Modifier.padding(20.dp)) {
        if(tagsState.set.isNotEmpty()){
            tagsState.set.forEach {
                Row{
                    Text(
                        text = it,
                        modifier = Modifier
                            .clickable { onNavigateToTagSearch(it) },
                        style = TextStyle(
                            textDecoration = TextDecoration.Underline,
                        ),
                        color = Color.Blue
                    )
                    Spacer(modifier = Modifier.size(10.dp))

                    Text(
                        text = "✗",
                        modifier = Modifier
                            .clickable {
                                tag.value = it
                                showDeleteTagDialog = true
                            },
                        style = TextStyle(
                            textDecoration = TextDecoration.Underline,
                        ),
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.size(15.dp))
            }
        }
        Text(
            text = "タグ追加",
            modifier = Modifier
                .clickable { showTagDialog = true },
            style = TextStyle(
                textDecoration = TextDecoration.Underline,
            )
        )
    }

    if(showDeleteTagDialog){
        DeleteTagDialog(bookInfo, tag.value, tagsState, bookDao )
    }
    if(showTagDialog){
        AddTagDialog(bookInfo, tagsState, bookDao)
    }
}


@Composable
private fun DeleteConfirm(bookInfo: BookInfo, bookDao: BookDao, navController: NavHostController){
    val openDialog = remember{ mutableStateOf(true) }
    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                Text(text = "「${bookInfo.book.title}」を削除しますか？")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    deleteBook(bookInfo.key, bookDao, navController)
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

private fun deleteBook(key:String, bookDao: BookDao,navController:NavHostController) {
    runBlocking {
        val job =launch { bookDao.deleteBook(key) }
        job.join()
        navController.navigateUp()
    }
}

@Composable
private fun AddTagDialog(
    bookInfo: MutableState<BookInfo?>, tagsState: TagsState, bookDao: BookDao
){
    val openDialog = remember{ mutableStateOf(true) }
    val (text, setText) = remember { mutableStateOf("")}
    val key = bookInfo.value!!.key

    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text(text ="タグ追加") },
            text = {
                TextField(
                    value = text, onValueChange = setText,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            openDialog.value = false
                            bookDao.addTagList(key,text)
                            tagsState.addSet(text)
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    bookDao.addTagList(key,text)
                    tagsState.addSet(text)
                }) {
                    Text("追加")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog.value = false
                }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

@Composable
private fun DeleteTagDialog(
    bookInfo: MutableState<BookInfo?>,
    tag: String,
    tagsState: TagsState,
    bookDao: BookDao
){
    val openDialog = remember{ mutableStateOf(true) }
    val key = bookInfo.value!!.key

    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text(text ="タグ削除") },
            text = { Text(text = "「$tag」を削除してよいですか")},
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    bookDao.deleteTag(key,tag)
                    tagsState.deleteSet(tag)
                }) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog.value = false
                }) {
                    Text("キャンセル")
                }
            }
        )
    }
}






@Composable
private fun rememberTagsState(): TagsState{
    return remember{
        TagsState()
    }
}

@Stable
class TagsState{
    val set = mutableSetOf<String>()

    fun replaceAllSet(tempSet:MutableSet<String>){
        set.clear()
        set.addAll(tempSet)
    }
    fun addSet(tag:String){
        set.add(tag)
    }
    fun deleteSet(tag: String){
        set.remove(tag)
    }

}