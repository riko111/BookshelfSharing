package com.isoffice.bookshelfsharing.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.util.CoilUtils.dispose
import com.google.zxing.ResultPoint
import com.isoffice.bookshelfsharing.MainActivity
import com.isoffice.bookshelfsharing.model.Book
import com.isoffice.bookshelfsharing.model.BookHttp
import com.isoffice.bookshelfsharing.model.OpenBD
import com.journeyapps.barcodescanner.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import timber.log.Timber
import kotlin.contracts.contract

@Composable
fun BarcodeScanScreen(navController: NavHostController) {
    val context = LocalContext.current
    var scanFlag by remember {
        mutableStateOf(false)
    }
    var barcode by remember{
        mutableStateOf("")
    }

    val compoundBarcodeView = remember {
        CompoundBarcodeView(context).apply {
            val capture = CaptureManager(context as Activity, this)
            capture.initializeFromIntent(context.intent, null)
            this.setStatusText("")
            capture.decode()
            this.decodeContinuous { result ->
                if (scanFlag) {
                    return@decodeContinuous
                }
                scanFlag = true
                result.text?.let { _barcode ->
                    scanFlag = false
                    barcode = _barcode
                }
            }
        }
    }

    Column {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.weight(0.7f)
                .border(width = 1.dp, color = Color.White)
        ) {
            AndroidView(
                modifier = Modifier,
                factory = { compoundBarcodeView },
            )
        }
        TextButton(
            onClick = { navController.navigate("book/$barcode") },
            modifier = Modifier.padding(5.dp)
        ) {
            Text(
                modifier= Modifier.fillMaxWidth(),
                text = barcode,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            )
        }
    }
    DisposableEffect(key1 = "someKey") {
        compoundBarcodeView.resume()
        onDispose { compoundBarcodeView.pause() }
    }
}





