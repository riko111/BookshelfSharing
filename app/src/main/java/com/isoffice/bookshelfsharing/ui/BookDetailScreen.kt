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
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.isoffice.bookshelfsharing.dao.BookDao
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.ui.viewModel.MainViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/*　本棚の本の詳細 */
@Composable
fun BookDetailScreen(key : String, bookDao: BookDao,viewModel: MainViewModel){
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
            BookContent(book.value!!, bookDao, viewModel)
        }
    }
}

@Composable
private fun BookContent(book: Book, bookDao: BookDao, viewModel: MainViewModel) {

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
                        if (book.publishedDate != null) Text(text = book.publishedDate)
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
        DeleteConfirm(book, bookDao, viewModel.navController!!)
    }
}

@Composable
private fun DeleteConfirm(book: Book, bookDao: BookDao, navController: NavHostController){
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
                    deleteBook(book.isbn!!, bookDao, navController)
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

private fun deleteBook(isbn:String, bookDao: BookDao,navController:NavHostController) {
    runBlocking {
        val job =launch { bookDao.deleteBook(isbn) }
        job.join()
        navController.navigateUp()
    }

}
