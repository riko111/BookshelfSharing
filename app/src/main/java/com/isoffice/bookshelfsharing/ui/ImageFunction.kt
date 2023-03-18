package com.isoffice.bookshelfsharing.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


fun getImageUri(context: Context): Uri {
    //MediaStore.ACTION_IMAGE_CAPTURE
//    val imagePath = File(context.cacheDir, "images")
//    imagePath.mkdirs()
    val imagePath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "images")
    imagePath.mkdirs()
    val date = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val file = File(imagePath, "$date.jpg")
    return FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )
}

/*
 写真を撮るか画像を選択するか（選択した写真が表示できないので保留）
 */
fun createChooser(uri:Uri): Intent {
    val getContentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "image/*"
    }

    val imageCaptureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

    val chooserIntent = Intent.createChooser(getContentIntent, "selectPhoto")
    chooserIntent.putExtra(
        Intent.EXTRA_INITIAL_INTENTS,
        arrayOf(imageCaptureIntent)
    )
    return chooserIntent
}
