package com.isoffice.bookshelfsharing.model

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.util.concurrent.TimeUnit

object BookHttp{
    private const val BOOK_JSON_URL = "https://api.openbd.jp/v1/get?isbn=%s"


    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    fun searchBook(q: String): OpenBD?{
        val request = Request.Builder()
            .url(String.format(BOOK_JSON_URL, q))
            .build()

        try {
            val response = client.newCall(request).execute()
            val tempString = response.body?.string()
            val json = tempString?.substring(1, tempString.length - 1) //最初と最後の配列の括弧を取る
            val formatter = Json { ignoreUnknownKeys = true }
            return formatter.decodeFromString(OpenBD.serializer(), json!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}