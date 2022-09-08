package com.isoffice.bookshelfsharing.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*　本棚の本の詳細 */
@Composable
fun BookDetailScreen(key : String, bookDao: BookDao, onNavigateToMain: () -> Unit){
    var book = remember { mutableStateOf<Book?>(null) }
    runBlocking {
        val job = launch { book = bookDao.readBook(key) }
        job.join()
    }
    Column(
        Modifier
            .padding(2.dp)
            .fillMaxWidth()
            ) {
        if(book.value == null){
            Box(modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }else {
            BookContent(book.value!!, bookDao, onNavigateToMain)
        }
    }
}

@Composable
private fun BookContent(book: Book, bookDao: BookDao, onNavigateToMain: () -> Unit) {

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
    Column (modifier = Modifier
        .padding(vertical = 2.dp)
        .fillMaxWidth()
        .border(1.dp, Color.LightGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Image(
                painter = painter,
                contentDescription = book.title,
                modifier = Modifier.width(200.dp),
                contentScale = ContentScale.Fit,
            )
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                )
                {
                    Image(
                        painter = icon, contentDescription = book.ownerId,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.size(3.dp))
                    Column(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .weight(fill = true, weight = 1f)
                    ) {
                        Text(text = book.title, fontWeight = FontWeight.Bold)
                        if (book.subtitle != null) Text(text = book.subtitle)
                        if (book.description != null) Text(text = book.description)
                        Spacer(modifier = Modifier.size(5.dp))
                        Text(text = book.author.toString())
                        Text(text = book.publisher.toString())
                        if (book.publishedDate != null) {
                            val date = dateFormatter(book.publishedDate.toString())
                            Text(text = date)
                        }
                        Spacer(modifier = Modifier.size(5.dp))
                    }
                }
                IconButton(onClick = { showDialog = true }
                ) {
                    Icon(
                        Icons.Sharp.Delete,
                        contentDescription = "",
                        tint = colorResource(id = com.isoffice.bookshelfsharing.R.color.brown_1),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
    }
    if(showDialog){
        DeleteConfirm(book, bookDao, onNavigateToMain)
    }
}


private fun dateFormatter(strDate : String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val date = LocalDate.parse(strDate, formatter)

    return DateTimeFormatter.ofPattern("yyyy/MM/dd").format(date)
}

@Composable
private fun DeleteConfirm(book: Book, bookDao: BookDao, onNavigateToMain: () -> Unit){
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
                    deleteBook(book.key!!, bookDao, onNavigateToMain)
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

private fun deleteBook(key:String, bookDao: BookDao,onNavigateToMain:() -> Unit) {
    bookDao.deleteBook(key)
    onNavigateToMain
}
