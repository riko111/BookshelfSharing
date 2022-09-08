package com.isoffice.bookshelfsharing.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Book
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.isoffice.bookshelfsharing.R

import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseUser
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.dao.BookOpenBDMapper
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookHttp
import com.isoffice.bookshelfsharing.model.OpenBD
import kotlinx.coroutines.*

/*
　openBD APIからの検索画面
 */
@Composable
fun BookContentScreen(barcode: String, user: FirebaseUser, bookDao: BookDao) {
    val context = LocalContext.current
    var book :OpenBD? = null
    runBlocking {
        val job = launch {
            book = withContext(Dispatchers.IO){
                BookHttp.searchBook(barcode)
            }
        }
        job.join()
    }
    if(book == null){
        Text(text = "該当する書籍がありません")
    } else {
        BookContent(item = book!!, user, bookDao, context)
    }
}
@Composable
private fun BookContent(item: OpenBD, user:FirebaseUser, bookDao: BookDao, context: Context) {
    val summary = item.summary
    val subtitle = item.onix.descriptiveDetail.titleDetail.titleElement.subtitle?.content
    val textContent = item.onix.collateralDetail.textContent
    val textMap = HashMap<String,String>()
    for (text in textContent){
        textMap[text.textType] = text.text
    }
    val book = BookOpenBDMapper.openBDToBook(item,user)
    var searchFlag by remember { mutableStateOf(false)  }
    runBlocking {
        val job1 = launch {
            searchFlag = bookDao.searchBookList(book)
        }
        job1.join()
    }
    var showDialog by remember { mutableStateOf(false) }
    val painter = if(summary.cover != null && summary.cover != "") {
        rememberAsyncImagePainter(summary.cover)
    } else {
        painterResource(id = R.drawable.ic_broken_image)
    }
    Column (modifier = Modifier
        .padding(vertical = 2.dp)
        .width(IntrinsicSize.Max)
        .border(1.dp, Color.LightGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

    ){
        Row(modifier = Modifier
            .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = summary.title,
                modifier = Modifier.width(150.dp),
                contentScale = ContentScale.Fit,
            )
            Column(modifier = Modifier
                .padding(vertical = 2.dp)
                .weight(fill = true, weight = 1f)) {
                Text(text = summary.title, fontWeight = FontWeight.Bold)
                if(subtitle != null) Text(text = subtitle)
                Spacer(modifier = Modifier.size(5.dp))
                Text(text = summary.author.toString())
                Text(text = summary.publisher.toString())
                Text(text = summary.pubdate.toString())
                Spacer(modifier = Modifier.size(5.dp))
                if(textMap.size != 0){
                    val map = textMap["02"]
                    if(map != null) {
                        Text(text = map)
                    }
                }
            }
        }
        if(searchFlag){
            OutlinedButton(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)) {
                Text(text = "本棚にあります", Modifier)
            }
        } else {
            OutlinedButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(3.dp)
            ) {
                Text(text = "本棚登録", Modifier)
            }
        }
    }
    if(showDialog){
        RegisteredAlert(item, user, bookDao,context)
    }
}


@Composable
fun RegisteredAlert(item: OpenBD, user: FirebaseUser,bookDao: BookDao, context: Context){
    val openDialog = remember{ mutableStateOf(true) }
    if(openDialog.value){
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = {
                val title = item.summary.title
                Text(text = "「${title}」を本棚に入れますか？")
            },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    registerBook(item, user, bookDao,context)
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

/*
 openBDの検索結果を本棚に追加する
 */
private fun registerBook(item: OpenBD, user: FirebaseUser, bookDao: BookDao, context: Context) {
    val book = BookOpenBDMapper.openBDToBook(item,user)
    runBlocking {
        val job2 = launch {
            bookDao.writeNewBook(book)
        }
        job2.join()
        Toast.makeText(context,"${book.title}を登録しました",Toast.LENGTH_LONG).show()
    }
}

