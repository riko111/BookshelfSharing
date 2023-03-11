package com.isoffice.bookshelfsharing.model

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.lang.Exception
import java.util.concurrent.TimeUnit

object BookHttp{
    private const val OPENBD_JSON_URL = "https://api.openbd.jp/v1/get?isbn=%s"
    private const val GOOGLEBOOKS_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s"


    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    fun searchBook(q: String): OpenBD?{
        val request = Request.Builder()
            .url(String.format(OPENBD_JSON_URL, q))
            .build()

        try {
            val response = client.newCall(request).execute()
            val tempString = response.body?.string()
            if(tempString != "[null]"){
                val json = tempString?.substring(1, tempString.length - 1) //最初と最後の配列の括弧を取る
                val formatter = Json { ignoreUnknownKeys = true }
                return formatter.decodeFromString(OpenBD.serializer(), json!!)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }


    fun searchBookByGoogle(q:String):GoogleBooks? {
        val request = Request.Builder()
            .url(String.format(GOOGLEBOOKS_URL, q))
            .build()
        try{
            val response = client.newCall(request).execute()
            val json = response.body?.string()
            val formatter = Json { ignoreUnknownKeys = true }
            return formatter.decodeFromString(GoogleBooks.serializer(), json!!)
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }
}